var period;
var api;
var max;
var port;
var settings = new Object();
var jsonString;
var retrievedObject;
var jsonSettings;

$(document).ready(() => { 
	
	$("#submit").click(Save);
	
	// Retrieve the object from storage
	retrievedObject = localStorage.getItem('settings');
	jsonSettings = JSON.parse(retrievedObject);
	
	$("#port").val(jsonSettings.port);
	$("#maxNumber").val(jsonSettings.max);
	$("#api").val(jsonSettings.api);
	$("#sampling").val(jsonSettings.period);

});

function Save(){
	period = $("#sampling").val();
	api = $("#api").val();
	max = $("#maxNumber").val();
	port = $("#port").val();
	
   settings.port = port;
   settings.max = max;
   settings.api = api
   settings.period = period;
   jsonString = JSON.stringify(settings);
	
	toFile();
}

function toFile(){
// Put the object into storage
localStorage.setItem('settings', jsonString);
}