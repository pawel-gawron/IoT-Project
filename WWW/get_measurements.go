package main

import (
	"encoding/json"
	"log"
	"net/http"
	"os/exec"
)

type Measurement struct {
	Value float32 `json:"value"`
	Unit  string  `json:"unit"`
}

type MeasCollection struct {
	Temperature Measurement `json:"temperature"`
	Humidity    Measurement `json:"humidity"`
	Pressure    Measurement `json:"pressure"`
	Roll        Measurement `json:"roll"`
	Pitch       Measurement `json:"pitch"`
	Yaw         Measurement `json:"yaw"`
}

func get_measurements(w http.ResponseWriter, r *http.Request) {
	if r.URL.Path != "/get_measurements" {
		http.Error(w, "404 not found", http.StatusNotFound)
		return
	}

	if r.Method != "GET" {
		http.Error(w, "Not supported method", http.StatusNotImplemented)
		return
	}

	w.Header().Set("Content-Type", "application/json")

	output, err := exec.Command("./get_measurements.py").Output()

	if err != nil {
		message := "Cannot get measurements"
		log.Printf("%s: %s\n", message, err)
		http.Error(w, message, http.StatusServiceUnavailable)
		return
	}

	var measurements_list []float32
	json.Unmarshal(output, &measurements_list)

	temperature := Measurement{Value: measurements_list[0], Unit: "C"}
	humidity := Measurement{Value: measurements_list[1], Unit: "%"}
	pressure := Measurement{Value: measurements_list[2], Unit: "hPa"}

	roll_position := Measurement{Value: measurements_list[3], Unit: "deg"}
	pitch_position := Measurement{Value: measurements_list[4], Unit: "deg"}
	yaw_position := Measurement{Value: measurements_list[5], Unit: "deg"}

	resp := MeasCollection{Temperature: temperature, Humidity: humidity, Pressure: pressure, Roll: roll_position, Pitch: pitch_position, Yaw: yaw_position}

	jsonResp, err := json.Marshal(resp)
	if err != nil {
		log.Printf("Error happened in JSON marshal. Err: %s\n", err)
	}

	w.Write(jsonResp)
}
