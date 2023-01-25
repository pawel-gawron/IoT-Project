/**
 * @brief Implementation of infinite impulse response (IIR) filter.
 */
class IIR_Filter{
	
	/**
	 * @brief Initialization of FIR filter algorithm. 
	 * @param ffc Array of FIR filter feedforward coefficients.
	 * @param st Array of FIR filter input initial state.
	 *        Must be the same size as coefficients array.
	 */
	 constructor(ffc, fbc, stf, stb) {
		this.feedforward_coefficients = ffc;
		this.stateforward = stf;
		this.feedbackward_coefficients = fbc;
		this.statebackward = stb;
		this.xf = 0;
	 }
	 
	/**
	 * @brief Execute one step of the FIR filter algorithm.
	 * @param x Input signal.
	 * @retval Output [filtered] signal.
	 */
	Execute(x) {
		// update state
		this.stateforward.unshift(x);
		this.stateforward.pop();

		this.statebackward.unshift(this.xf);
		this.statebackward.pop();
		// compute output
		this.xf = 0.0;
		for (let i = 0; i < this.stateforward.length; i++) {
			if (i == 0){	
				this.xf = this.xf + (this.feedforward_coefficients[i]*this.stateforward[i]);
			}
			else
			{
				this.xf = this.xf + (this.feedforward_coefficients[i]*this.stateforward[i] - this.feedbackward_coefficients[i]*this.statebackward[i-1]);
			}
		}
		return this.xf;
	}
}