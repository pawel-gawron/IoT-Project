async function fetchAsync (url) {
  let response = await fetch(url);
  let data = await response.json();
  return data;
}

class server {
	constructor(ip){
		this.protocol = 'http://';
		this.ip = ip + '/';
		this.script = 'get_measurements'; // 'cgi-bin/server/test_signal.py'
		this.signalValue = 0;
		this.temp = 0;
	}
	async getTestSignal(sampleNumber) {
		this.temp = fetchAsync("http://192.168.0.44:8080/get_measurements");
		// await $.get(this.protocol + this.ip + this.script
			// (response) => { this.signalValue = response}
					// await $.get('http://192.168.0.44:8080/get_measurements'
			// (response) => { this.signalValue = response}
		// )
		this.temp.then((value) =>{
			// console.log(value["pressure"]);
			return value["pressure"];
		});
		// return this.temp[0];
	}
}
