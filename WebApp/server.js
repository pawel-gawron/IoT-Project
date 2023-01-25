class server {
	constructor(ip){
		this.protocol = 'http://';
		this.ip = ip + '/';
		this.script = 'system_iot/temperature_pressure.php?k='; // 'cgi-bin/server/test_signal.py'
		this.signalValue = 0;
	}
	async getTestSignal(sampleNumber) {

		await $.get(this.protocol + this.ip + this.script + sampleNumber,
			(response) => { this.signalValue = response}
		)
		console.log(`signal = ${this.signalValue['press']}`);
		return this.signalValue;
	}
}
