var period;
var ip;
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

	$("#ip_address").val(jsonSettings.ip_address);
	$("#port").val(jsonSettings.port);
	$("#maxNumber").val(jsonSettings.max);
	$("#api").val(jsonSettings.api);
	$("#sampling").val(jsonSettings.period);

});

function Save(){
	ip = $("#ip_address").val();
	period = $("#sampling").val();
	api = $("#api").val();
	max = $("#maxNumber").val();
	port = $("#port").val();

	 settings.ip_address = ip;
   settings.port = port;
   settings.max = max;
   settings.api = api
   settings.period = period;
   jsonString = JSON.stringify(settings);

	 // sessionStorage.setItem("ip_address:, "ip");;
	 sessionStorage.setItem("ip_address", ip);;
	 var ip_address = sessionStorage.getItem("ip_address");
	 console.log(ip_address);
	 globalThis.yourGlobalVariable = ip;
	 console.log(globalThis.yourGlobalVariable);

	toFile();
}

function toFile(){
// Put the object into storage
localStorage.setItem('settings', jsonString);
}
