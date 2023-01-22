use eframe::egui::{
    self,
    plot::{Legend, Line, Plot, PlotPoints},
    Ui,
};
use serde::{Deserialize, Serialize};
use std::{collections::HashMap, fs, sync::mpsc, thread, time::Duration};

fn main() {
    let options = eframe::NativeOptions {
        initial_window_size: Some(egui::vec2(640.0, 480.0)),
        ..Default::default()
    };

    let app = Box::new(App::new());
    eframe::run_native("Projekt AIR", options, Box::new(|_cc| app));
}

enum GetDataMode {
    Off,
    On,
    Stop,
}

#[derive(PartialEq, Eq)]
enum Panel {
    Plot,
    Led,
    Table,
    Settings,
}

#[derive(Clone, Copy, Debug)]
struct Sample {
    time: f64,
    orientation: Orientation,
}

#[derive(Serialize, Deserialize, Clone, Copy, Debug)]
struct Orientation {
    roll: f32,
    pitch: f32,
    yaw: f32,
}

#[derive(Serialize, Deserialize, Clone, Debug)]
struct MeasurementsCollection {
    temperature: Measurement,
    humidity: Measurement,
    pressure: Measurement,
    roll: Measurement,
    pitch: Measurement,
    yaw: Measurement,
}

#[derive(Serialize, Deserialize, Clone, Debug)]
struct Measurement {
    value: f32,
    unit: String,
}

impl Default for Measurement {
    fn default() -> Self {
        Self {
            value: 0.0,
            unit: String::new(),
        }
    }
}

#[derive(Serialize, Deserialize)]
struct Settings {
    ip_address: String,
    port: u32,
}

#[derive(Serialize, Deserialize, Copy, Clone)]
struct Color {
    R: u8,
    G: u8,
    B: u8,
    state: bool,
}

impl Default for Color {
    fn default() -> Self {
        Color {
            R: 0,
            G: 0,
            B: 0,
            state: false,
        }
    }
}

struct App {
    open_panel: Panel,

    _timer_thread: thread::JoinHandle<()>,
    tx: mpsc::Sender<GetDataMode>,
    rx: mpsc::Receiver<Sample>,
    max_samples: usize,
    samples: Vec<Sample>,
    sampl_time: String,
    sampl_time_tx: mpsc::Sender<u64>,

    saved_flag: bool,

    colors_array: [[f32; 3]; 64],

    ip_address: String,
    port: String,
    tx_url: mpsc::Sender<String>,
}

impl eframe::App for App {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        egui::CentralPanel::default().show(ctx, |ui| {
            ui.horizontal(|ui| {
                ui.selectable_value(&mut self.open_panel, Panel::Plot, "Plot orientation");
                ui.selectable_value(&mut self.open_panel, Panel::Led, "LED matrix");
                ui.selectable_value(&mut self.open_panel, Panel::Table, "Measurements");
                ui.selectable_value(&mut self.open_panel, Panel::Settings, "Settings");
            });

            match self.open_panel {
                Panel::Plot => self.plot_orientation(ui),
                Panel::Led => self.led_matrix(ui),
                Panel::Table => self.table(ui),
                Panel::Settings => self.settings(ui),
            }

            ctx.request_repaint();
        });
    }
}

impl Drop for App {
    fn drop(&mut self) {
        self.tx.send(GetDataMode::Stop).unwrap();
    }
}

impl App {
    fn new() -> Self {
        let (tx, rx) = mpsc::channel();
        let (tx_data, rx_data) = mpsc::channel();
        let (tx_time, rx_time) = mpsc::channel();
        let (tx_url, rx_url) = mpsc::channel();

        let mut ip_address = String::new();
        let mut port = String::new();

        if let Ok(settings_content) = fs::read_to_string("settings.json") {
            if let Ok(settings) = serde_json::from_str::<Settings>(settings_content.as_str()) {
                ip_address = settings.ip_address;
                port = settings.port.to_string();
            }
        }

        let mut row = HashMap::new();
        row.insert("0".to_string(), Color::default());
        row.insert("1".to_string(), Color::default());
        row.insert("2".to_string(), Color::default());
        row.insert("3".to_string(), Color::default());
        row.insert("4".to_string(), Color::default());
        row.insert("5".to_string(), Color::default());
        row.insert("6".to_string(), Color::default());
        row.insert("7".to_string(), Color::default());

        let mut color_settings = HashMap::new();
        color_settings.insert("0".to_string(), row.clone());
        color_settings.insert("1".to_string(), row.clone());
        color_settings.insert("2".to_string(), row.clone());
        color_settings.insert("3".to_string(), row.clone());
        color_settings.insert("4".to_string(), row.clone());
        color_settings.insert("5".to_string(), row.clone());
        color_settings.insert("6".to_string(), row.clone());
        color_settings.insert("7".to_string(), row.clone());

        let mut colors_array = [[1.0f32; 3]; 64];
        let reponse = reqwest::blocking::get(format!("http://{}:{}/get_color", &ip_address, &port));
        if let Ok(resp) = reponse {
            color_settings = resp
                .json::<HashMap<String, HashMap<String, Color>>>()
                .unwrap();

            for i in 0..8 {
                for j in 0..8 {
                    let color = color_settings[&i.to_string()][&j.to_string()];
                    colors_array[i + 8 * j] = [
                        color.R as f32 / 255.0,
                        color.G as f32 / 255.0,
                        color.B as f32 / 255.0,
                    ];
                }
            }
        }

        tx_url
            .send(format!("http://{}:{}", ip_address, port))
            .unwrap();

        Self {
            open_panel: Panel::Plot,
            colors_array,
            _timer_thread: thread::spawn(move || {
                let mut plot_flag = GetDataMode::Off;
                let mut time = 0.0;
                let mut sampling_time: u64 = 1000;
                let mut url = String::new();

                loop {
                    if let Ok(new_flag) = rx.try_recv() {
                        plot_flag = new_flag;
                    }

                    if let Ok(new_url) = rx_url.try_recv() {
                        url = new_url;
                    }

                    match plot_flag {
                        GetDataMode::On => {
                            let reponse =
                                reqwest::blocking::get(format!("{}/get_measurements", url));
                            if let Ok(resp) = reponse {
                                let measurements = resp.json::<MeasurementsCollection>().unwrap();
                                let orientation = Orientation {
                                    roll: measurements.roll.value,
                                    pitch: measurements.pitch.value,
                                    yaw: measurements.yaw.value,
                                };
                                tx_data.send(Sample { time, orientation }).unwrap();
                            }

                            time += sampling_time as f64 / 1000.0;
                        }
                        GetDataMode::Stop => {
                            break;
                        }
                        GetDataMode::Off => {}
                    }

                    if let Ok(new_time) = rx_time.try_recv() {
                        sampling_time = new_time;
                    }

                    thread::sleep(Duration::from_millis(sampling_time));
                }
            }),
            tx,
            rx: rx_data,
            max_samples: 30,
            samples: Vec::new(),
            sampl_time: String::from("1000"),
            sampl_time_tx: tx_time,
            saved_flag: false,
            ip_address,
            port,
            tx_url,
        }
    }

    fn plot_orientation(&mut self, ui: &mut Ui) {
        ui.heading("Plot - orientation");
        ui.horizontal(|ui| {
            if ui.button("Start").clicked() {
                self.tx.send(GetDataMode::On).unwrap();
            }
            if ui.button("Stop").clicked() {
                self.tx.send(GetDataMode::Off).unwrap();
            }
        });

        ui.horizontal(|ui| {
            ui.label("Sampling time [ms]:");
            ui.add(egui::TextEdit::singleline(&mut self.sampl_time).desired_width(80.0));
            if ui.button("Submit").clicked() {
                if let Ok(sample_time) = self.sampl_time.parse() {
                    self.sampl_time_tx.send(sample_time).unwrap();
                }
            }
        });

        if let Ok(sample) = self.rx.try_recv() {
            if self.samples.len() < self.max_samples {
                self.samples.push(sample);
            } else {
                for i in 0..self.samples.len() - 1 {
                    self.samples[i] = self.samples[i + 1];
                }

                let last_element = self.samples.last_mut().unwrap();
                *last_element = sample;
            }
        }

        let roll_plot: PlotPoints = self
            .samples
            .iter()
            .map(|sample| [sample.time, sample.orientation.roll as f64])
            .collect();

        let pitch_plot: PlotPoints = self
            .samples
            .iter()
            .map(|sample| [sample.time, sample.orientation.pitch as f64])
            .collect();

        let yaw_plot: PlotPoints = self
            .samples
            .iter()
            .map(|sample| [sample.time, sample.orientation.yaw as f64])
            .collect();

        let roll_line = Line::new(roll_plot).name("roll");
        let pitch_line = Line::new(pitch_plot).name("pitch");
        let yaw_line = Line::new(yaw_plot).name("yaw");

        Plot::new("Orientation")
            .view_aspect(2.0)
            .width(600.0)
            .legend(Legend::default())
            .show(ui, |plot_ui| {
                plot_ui.line(roll_line);
                plot_ui.line(pitch_line);
                plot_ui.line(yaw_line);
            });
    }

    fn led_matrix(&mut self, ui: &mut Ui) {
        ui.heading("LED matrix");

        for y in 0..8 {
            ui.horizontal(|ui| {
                for x in 0..8 {
                    if ui
                        .color_edit_button_rgb(&mut self.colors_array[y * 8 + x])
                        .changed()
                    {
                        self.saved_flag = false;
                    }
                }
            });
        }

        if ui.button("Submit").clicked() {
            let mut row = HashMap::new();
            row.insert("0".to_string(), Color::default());
            row.insert("1".to_string(), Color::default());
            row.insert("2".to_string(), Color::default());
            row.insert("3".to_string(), Color::default());
            row.insert("4".to_string(), Color::default());
            row.insert("5".to_string(), Color::default());
            row.insert("6".to_string(), Color::default());
            row.insert("7".to_string(), Color::default());

            let mut color_settings = HashMap::new();
            color_settings.insert("0".to_string(), row.clone());
            color_settings.insert("1".to_string(), row.clone());
            color_settings.insert("2".to_string(), row.clone());
            color_settings.insert("3".to_string(), row.clone());
            color_settings.insert("4".to_string(), row.clone());
            color_settings.insert("5".to_string(), row.clone());
            color_settings.insert("6".to_string(), row.clone());
            color_settings.insert("7".to_string(), row.clone());

            for x in 0..8 {
                for y in 0..8 {
                    color_settings
                        .get_mut(&x.to_string())
                        .unwrap()
                        .get_mut(&y.to_string())
                        .unwrap()
                        .R = (self.colors_array[y * 8 + x][0] * 255.0) as u8;
                    color_settings
                        .get_mut(&x.to_string())
                        .unwrap()
                        .get_mut(&y.to_string())
                        .unwrap()
                        .G = (self.colors_array[y * 8 + x][1] * 255.0) as u8;
                    color_settings
                        .get_mut(&x.to_string())
                        .unwrap()
                        .get_mut(&y.to_string())
                        .unwrap()
                        .B = (self.colors_array[y * 8 + x][2] * 255.0) as u8;
                    color_settings
                        .get_mut(&x.to_string())
                        .unwrap()
                        .get_mut(&y.to_string())
                        .unwrap()
                        .state = true;
                }
            }

            let json_str = serde_json::to_string(&color_settings).unwrap();

            let url = format!(
                "http://{}:{}/set_color?color_settings={}",
                self.ip_address, self.port, json_str
            );

            let _ = reqwest::blocking::get(url.as_str());

            self.saved_flag = true;
        }

        if self.saved_flag {
            ui.label("Saved");
        }
    }

    fn settings(&mut self, ui: &mut Ui) {
        ui.heading("Settings");

        ui.horizontal(|ui| {
            ui.label("IP address:");
            ui.add(egui::TextEdit::singleline(&mut self.ip_address).desired_width(80.0));
        });

        ui.horizontal(|ui| {
            ui.label("Port:");
            ui.add(egui::TextEdit::singleline(&mut self.port).desired_width(80.0));
        });

        if ui.button("Submit").clicked() {
            if let Ok(port) = self.port.parse::<u32>() {
                let settings = Settings {
                    ip_address: self.ip_address.to_string(),
                    port,
                };
                let settings_json = serde_json::to_string(&settings).unwrap();
                fs::write("settings.json", settings_json).unwrap();
                self.tx_url
                    .send(format!("http://{}:{}", self.ip_address, self.port))
                    .unwrap();
            }
        }
    }

    fn table(&mut self, ui: &mut Ui) {
        ui.heading("Measurements");

        let url = format!("http://{}:{}/get_measurements", self.ip_address, self.port);
        let response = reqwest::blocking::get(url.as_str());

        let mut temperature = Measurement::default();
        let mut humidity = Measurement::default();
        let mut pressure = Measurement::default();
        let mut roll = Measurement::default();
        let mut pitch = Measurement::default();
        let mut yaw = Measurement::default();

        if let Ok(resp) = response {
            let measurements = resp.json::<MeasurementsCollection>().unwrap();

            temperature = measurements.temperature;
            humidity = measurements.humidity;
            pressure = measurements.pressure;
            roll = measurements.roll;
            pitch = measurements.pitch;
            yaw = measurements.yaw;
        }

        ui.label(format!(
            "Temperature: {:.2} {}",
            temperature.value, temperature.unit
        ));
        ui.label(format!("Humidity: {:.2} {}", humidity.value, humidity.unit));
        ui.label(format!("Pressure: {:.2} {}", pressure.value, pressure.unit));
        ui.label(format!("Roll: {:.2} {}", roll.value, roll.unit));
        ui.label(format!("Pitch: {:.2} {}", pitch.value, pitch.unit));
        ui.label(format!("Yaw: {:.2} {}", yaw.value, yaw.unit));
    }
}
