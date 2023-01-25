// var buttonCounter = 0; ///< button onClick event counter
// /* @brief button onClick event handling */
// function myOnClickMethod() {
// buttonCounter += 1;
// var paragraphDispText = "<b>Click counter: </b>" + buttonCounter.toString();
// document.getElementById("paragraph").innerHTML = paragraphDispText;
// }

// initialize LED matrix json object,
class Led {
  constructor() {
    this.R = 0;
    this.G = 0;
    this.B = 0;
    this.state = false;
  }
}

var matrixState = {}
for (let row = 0; row < 8; row++) {
    matrixState[row] = {}
    for (let col = 0; col<8; col++) {
        matrixState[row][col] = new Led;
    }
}
console.log(JSON.stringify(matrixState, null, 2));

// Color picker
let colorWell;
const defaultColor = "#0000ff";

window.addEventListener("load", startup, false);


function startup() {
  colorWell = document.querySelector("#colorWell");
  colorWell.value = defaultColor;
  colorWell.addEventListener("input", updateFirst, false);
  // colorWell.addEventListener("change", updateAll, false);
  colorWell.select();
}

var color;
var r;
var g;
var b;
function updateFirst(event) {
  // const p = document.querySelector("p");
  // if (p) {
    color = event.target.value;
    r = parseInt(color.substr(1,2), 16)
    g = parseInt(color.substr(3,2), 16)
    b = parseInt(color.substr(5,2), 16)
    console.log(`red: ${r}, green: ${g}, blue: ${b}`)
}



function updateLED(buttonID)
{
  var row = (buttonID - buttonID%10)/10;
  var column = buttonID%10;

  // var debugText = "button id: " + buttonID.toString();
  // document.getElementById("paragraph").innerHTML = debugText;

  matrixState[row][column]['state'] = !matrixState[row][column]['state'];

  var isON = (matrixState[row][column]['state']);
  // matrixState[row][column]['state'] = isON  ? 1 : 0;

  console.log(`row=${row}, column=${column}, state=${isON}`);


  var buttonColor = isON ? color : '';


  // var color = document.getElementById(colorpicker)
  console.log(`Colorpicker: ${color}`)
  matrixState[row][column]['R'] = r;
  matrixState[row][column]['G'] = g;
  matrixState[row][column]['B'] = b;
  document.getElementById(buttonID).style.background = buttonColor;

  document.getElementById(buttonID).innerHTML = isON? 1 : 0;
  // document.getElementById("debug").innerHTML = matrixState[buttonID]['O'];
  // document.getElementById("state_json").innerHTML = JSON.stringify(matrixState, null, 2);
}
//
function download(content, fileName, contentType) {
    var a = document.createElement("a");
    var file = new Blob([content], {type: contentType});
    a.href = URL.createObjectURL(file);
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(a.href);
}

download(jsonData, 'json.txt', 'text/plain');

async function fetchAsync (url) {
  let response = await fetch(url);
  let data = await response.json();
  return data;
}

function userSendData()
{
  // download( JSON.stringify(matrixState, null, 2), 'matrix.json', 'text/plain');
  //
  // localStorage.setItem('LED_json', JSON.stringify(matrixState, null, 2));
  // // $("#response").val(JSON.stringify(matrixState, null, 2));
  //
  // if(localStorage.getItem('LED_json'))
  // {
	//   $("#response").val("Data on server");
	//   // $("#data").val(JSON.stringify(matrixState, null, 2));
  // }
  var json_string = JSON.stringify(matrixState, null, 2).replace(/\s/g, '');
  console.log(json_string);
  // require('http');
  // var client = new HttpClient();
  // client.get(`http://localhost:8080/set_color?color_settings=${json_string}`, function(response) {
  //   console.log(response);
  // });
  console.log(fetchAsync(`http://192.168.0.44:8080/set_color?color_settings=${json_string}`));
  //   // do something with response
};
