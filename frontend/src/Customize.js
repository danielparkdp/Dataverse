import React, { Component } from 'react';
import './css/Customize.scss';
import rocket_purple from "./img/rockets/rocket_purple.png";
import rocket_red from "./img/rockets/rocket_red.png";
import rocket_blue from "./img/rockets/rocket_blue.png";
import rocket_gold from "./img/rockets/rocket_gold.png";
import rocket_pink from "./img/rockets/rocket_pink.png";
import rocket_green from "./img/rockets/rocket_green.png";


/**
 * Models a 'Customize' object, which allows the user to select a rocket
 * from the rockets they have already unlocked.
 *
 * @prop close, function to close the component
 * @prop options, rockets avaliable to the user
 *
 */
class Customize extends Component {


    constructor(props) {
        super(props);

        this.state = {
            rocket_list: this.ownedRockets(this.props.options),
            curr: 0
        }
    }

    ownedRockets = (rockets) => {
        let r = [];

        for (let ele in rockets) {
            if (rockets[ele] === 1 || rockets[ele] === 2) {
                if (ele === "red") {
                  r.push(rocket_red);
                } else if (ele === "blue") {
                  r.push(rocket_blue);
                } else if (ele === "purple") {
                  r.push(rocket_purple);
                } else if (ele === "gold") {
                  r.push(rocket_gold);
                } else if (ele === "green") {
                  r.push(rocket_green);
                } else if (ele === "pink") {
                  r.push(rocket_pink);
                }
            }
        }
        return r;
    };


    right = () => {
        const curr = this.state.curr;
        if (curr < this.state.rocket_list.length - 1) {
            this.setState({curr: curr+1});
        } else {
            this.setState({curr: 0});

        }
    };

    left = () => {
        const curr = this.state.curr;
        if (curr > 0) {
            this.setState({curr: curr-1});
        } else {
            this.setState({curr: this.state.rocket_list.length - 1});

        }
    };

    onKeyPress = (event) => {
        switch(event.keyCode) {
            case 37:
                this.left();
                break;
            case 39:
                this.right();
                break;
        }
    };


    toRocketString = (rocket) => {
        switch (rocket) {
            case rocket_blue:
                return "blue";
            case rocket_red:
                return "red";
            case rocket_purple:
                return "purple";
            case rocket_gold:
                return "gold";
            case rocket_green:
                return "green";
            case rocket_pink:
                return "pink";
        }
    };

    componentDidMount() {
        document.getElementById("Customize").focus();
    }

    render() {
        return (
            <div id={"Customize"} className="Customize" onKeyDown={this.onKeyPress}>
                <button className={"close-button"} id={"customize-close"} onClick={this.props.close}>
                    <i className="fas fa-times"></i> </button>
                <h2>Choose your rocket!</h2>
                <h4>Number of rockets owned: {this.state.rocket_list.length}</h4>
                <div className={"rocket-select-div"}>
                    {(this.state.rocket_list.length !== 1) ? <i id={"customize-left"} className="arrow left grow" ref="leftArrow" onClick={this.left}></i>: null}
                    <div className={"rocket"} onClick={this.right} style={ { backgroundImage:`url(${this.state.rocket_list[this.state.curr]})` }}>
                    </div>
                    {(this.state.rocket_list.length !== 1) ? <i id={"customize-right"} className="arrow right grow" ref="rightArrow" onClick={this.right}></i>: null}
                </div>
                <button className={"rocket-submit"} onClick={() => this.props.close(this.toRocketString(this.state.rocket_list[this.state.curr]))}>Choose Rocket</button>
            </div>
        );
    }
}


export default Customize;
