json_data = {
    "Temperature":{

        "value": 25.5,
        "unit": "C"
    },
    "Humidity":{

        "value": 98.3,
        "unit": "%"
    },
    "Pressure":{
        "value": 1024,
        "unit": "mbar"
    },
    "Roll position":{
        "value": 45.6,
        "unit": "deg"
    },
    "Pitch position":{
        "value": 12.3,
        "unit": "deg"
    },
    "Yaw position":{
        "value": 78.9,
        "unit": "deg"
    }
}
buildTable(json_data)

function buildTable(data){
  var table = document.getElementById('myTable')
  for (var key in data ){
    if (data.hasOwnProperty(key)) {
    var row = `<tr>
                  <td>${key}</td>
              </tr>`
    table.innerHTML += row
  }
}
}
