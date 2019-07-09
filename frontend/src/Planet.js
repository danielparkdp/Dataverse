import React, { Component } from 'react';
import './css/World.scss';

/**
 * Models a planet in the universe.
 *
 * Expects the following props:
 * @prop rotate, rotation of the planet image (optional)
 * @prop name, name of the planet to apear on hover.
 * @prop top, left: x, y coordinates of the planet.
 * @prop imgUrl, image for this planet.
 * @prop id, id for the div for this planet.
 */
class Planet  extends Component {

    //represents a planet in the solar system
    constructor(props) {
        super(props);
        this.props = {
            name: "",
            imgUrl: "",
            top: 0,
            left: 0,
        };

        this.state = {
            hover: false
        }
    }

    //show label on hover
    toggleHover = () => {
        this.setState({hover: !this.state.hover});
    };

    render() {
        //sets style of planet based on unique prop values/changes
        let rotate = this.props.rotate ? this.props.rotate + "deg" : "0deg"; 
        const style = {
            width: this.props.width,
            transform: `rotate(${rotate})`
        };
        return (
            <div className="planet-wrapper" style={{top: (this.props.top) + "px", left: this.props.left + "px"}}>
                <img className="planet" src={this.props.imgUrl} onMouseEnter={this.toggleHover}
                    onMouseLeave={this.toggleHover} id={this.props.id} style={style}>
                </img>
                {this.state.hover? <div className={"hover"}>{this.props.hover}</div> : null}
            </div>
        );
    }
}

export default Planet;
