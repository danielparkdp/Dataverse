import React, { Component } from 'react';
import '../css/World.scss';

/**
 * A timer that can be used in any game.  Counts down from given start time.
 * 
 * Expects props
 *     @prop startTime : initial time of timer countdown
 *     @prop onEnd : () => {} function called when countdown is up.
 */
class Timer extends Component {

    constructor(props) {
        super(props);
        this.props = {
            startTime: 60, //start time
        }
        this.state = {
            time: 60, //curent time
        };

        this.timerNumText = React.createRef();
    }

    //sets the start ime
    componentDidMount() {
        this.setState({time: this.props.startTime});
        this.start();
    }

    //starts the timer
    start() {
        setTimeout(() => this.countDown(), 1000);
    }

    //ticks the timer down by 1 second
    countDown(){
        let nextTime = this.state.time - 1;
        this.setState({time: nextTime});
        //if timer is up, calls onEnd() function
        if(nextTime <= 0){
            if(this.props.onEnd && this.timerNumText.current){
                this.timerNumText.current.classList.remove("apple-red-color");
                this.props.onEnd();
            }
        }  else {
            //change color to red if less than 5 seconds left
            if(nextTime <= 5 && this.timerNumText.current){
                this.timerNumText.current.classList.add("apple-red-color");
            }
            setTimeout(() => this.countDown(), 1000);
        }
    }

    render() {
        return (
            <div className="score-box time-box"> TIME
              <h2 id="timer-num-text" ref = {this.timerNumText} style={{fontSize: "32px"}}>{this.state.time}</h2>
            </div>
        );
    }
}



export default Timer
