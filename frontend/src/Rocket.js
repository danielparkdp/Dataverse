import React, { Component } from 'react';
import "./css/Rocket.scss";


/**
 * Models the rocket controlled by the player.
 *
 * Expects the following proos:
 *  @prop degree - current rotation of the rocket
 *  @prop x, y - x and y position of the rocket
 *  @prop image: pre-imported rocket image to display.
 */
class Rocket extends Component {

    constructor(props) {
        super(props);
    }

    render() {

        const degree = this.props.degree.toString() + "deg";
        const transX = this.props.x.toString() + "px";
        const transY = this.props.y.toString() + "px";
        const style = {
            transform: `translate(${transX}, ${transY}) rotate(${degree}) `,
            backgroundImage: `url(${this.props.img})`


    };


        return (
            <div className="Rocket" id="rocket" onKeyDown={this.onKeyDown} style ={style}>
            </div>
        );
    }
}


export default Rocket;
