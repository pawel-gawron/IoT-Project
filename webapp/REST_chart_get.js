var startSampling;
var time;          //!< Timestamps array
var signal;        //!< Original and filtered signals arrays
var signal2;

var samplesMax;
var k = 0; //!< Samples counter
var chartTimer = null;
var x;
var y;
var filter;
var filter2;
var ip_address = "192.168.1.66";

var signal_name = "temperature"
var unit = "C"
/**** My IIR Low pass filter ****************************************************/

$(document).ready(() => {

	startSampling = $("#samplingPeriod").val();
	ChartInit();

	$("#start").click(Start);
	$("#stop").click(Stop);

	// $("#samplefreq").text(1.0/MyFirData.sampletime);

	// serverMock = new server("192.168.1.64:8080");
	filter2 = new IIR_Filter(IIRFirData.feedforward_coefficients, IIRFirData.feedbackward_coefficients, IIRFirData.stateforward, IIRFirData.statebackward);
	filter = new MyFir(MyFirData.feedforward_coefficients, MyFirData.state);
});

async function fetchAsync (url) {
  let response = await fetch(url);
  let data = await response.json();
  return data;
}

async function getJsonData() {
	if( k <= samplesMax ){
		// get signal from server
		// x = serverMock.getTestSignal(k);
		// ip_address = sessionStorage.getItem("ip_address");
		var ip_address = sessionStorage.getItem("ip_address");
		if (!ip_address){
			ip_address = "192.168.1.66"
		}
		console.log(ip_address);

		x = fetchAsync(`http://${ip_address}:8080/get_measurements`);

		x.then((value) =>{
			// console.log(value["pressure"]);
			x = value;
			unit = x[signal_name]["unit"]
			xf = filter.Execute(x[signal_name]["value"]);
			signal[0].push(x[signal_name]["value"]);
			// signal[1].push(x.press);
			signal[1].push(xf);

		// x = await serverMock.getTestSignal(k)
		// filter signal
		$("#response").val(JSON.stringify(x));
		// display data (Chart.js)
		// console.log(`temp: ${x.temp}`);
		chart.update();
		});

		// // update time
		k++;
	} else {
		clearInterval(chartTimer);
		chartTimer = null;
	}
}

function Start(){

	startSampling = $("#samplingPeriod").val();
	ChartInit();
	samplesMax = time.length;

	if (chartTimer == null){
		k = 0;
		signal[0].splice(0,signal[0].length);
		signal[1].splice(0,signal[1].length);


		chart.update();

		chartTimer = setInterval(getJsonData, startSampling * 1000);
	}
}

function Stop(){
	if (chartTimer != null){
		clearInterval(chartTimer);
		chartTimer = null;
	}
}

function change_signal(){
var e = document.getElementById("choose_signal");
signal_name = e.value;
ChartInit();

}

function capitalize(s)
{
    return s[0].toUpperCase() + s.slice(1);
}
function ChartInit()
{
	// array with consecutive integers: <0, maxSamplesNumber-1>
	time = [...Array(Math.round(10/startSampling)).keys()];
	// scaling all values ​​times the sample time
	time.forEach(function(p, i) {this[i] = (this[i]*startSampling).toFixed(1);}, time);

	// get chart context from 'canvas' element
	chartContext = $("#chart")[0].getContext('2d');

	Chart.defaults.global.elements.point.radius = 1;
	name = capitalize(signal_name);
	chart = new Chart(chartContext, {
		// The type of chart: linear plot
		type: 'line',

		// Dataset: 'xdata' as labels, 'signal1' as dataset.data
		data: {
			labels: time,
			datasets: [{
				fill: false,
				label: 'Unfiltered',
				backgroundColor: 'rgb(0, 255, 0)',
				borderColor: 'rgb(0, 255, 0)',
				data: [],
				lineTension: 0
				// ,
				// yAxisID: 'yTemp'
			},
			{
				fill: false,
				label: 'Filtered',
				backgroundColor: 'rgb(0, 0, 255)',
				borderColor: 'rgb(0, 0, 255)',
				data: [],
				lineTension: 0
				// ,
				// yAxisID: 'yPress'
			}]
		},

		// Configuration options
		options: {
			responsive: true,
			maintainAspectRatio: false,
			animation: false,
			scales: {
				yAxes: [
				// {
					// scaleLabel: {
						// display: true,
						// labelString: 'Pressure [hPa]',
					// },
					// ticks: {
						// suggestedMin: 900,
						// suggestedMax: 1200
					// },
					// position: 'right',
					// id: "yPress"
				// },
				{
					scaleLabel: {
						display: true,
						labelString: `${name} [${unit}]`
					},
					ticks: {
						suggestedMin: 0,
						suggestedMax: 33
					}
					// ,
					// position: 'left',
					// id: "yTemp"
				}],
				xAxes: [{
					scaleLabel: {
						display: true,
						labelString: 'Time [s]'
					},
					ticks: {
						suggestedMin: 0,
						suggestedMax: 100
					}
				}]
			}
		}
	});


	signal = [chart.data.datasets[0].data,
			  chart.data.datasets[1].data];

	time = chart.data.labels

}
