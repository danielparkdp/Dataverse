import React, { Component } from 'react';
import './css/Tutorial.scss';
import red_rocket_img from './img/rockets/rocket_red.png'
import planet_img from './img/planets/cookie_planet.png'
import arena_img from './img/planets/arena_planet.png'


class Menu extends Component {

    closeOnClick(event) {
        if(event) event.preventDefault();
        this.props.onClose();
    }

    render() {
        return (
            <div className="tut-scroll-div">
            <div className="tutorial" >
                <button className={"close-button"} id={"tut-close"} onPointerDown={(event) => this.closeOnClick(event)}>
                    <i className="fas fa-times"></i> </button>

                <h2 id={"tut-title"}> Welcome to dataverse! </h2>

                <div className="grid-container">
                    {/* HOW TO FLY */}
                    <div className="grid-item" id="tut-rocket-cell"><img className={"tut-pic"} id="tut-rocket-img" src={red_rocket_img}/></div>
                    <div className="grid-item" id="tut-rocket-text"><div className={"tut-text"}>Fly your rocket around with arrow keys</div></div>

                     {/* HOW TO PLAY GAMES */}
                    <div className="grid-item" id="tut-planet-text"><div className={"tut-text"}>Land on a planet to play a game about a data structure </div></div>
                    <div className="grid-item" id="tut-planet-cell"><img className={"tut-pic"} id="tut-planet-img" src={planet_img}/></div>

                     {/* ARENA */}
                    <div className="grid-item" id="tut-arena-cell"><img className={"tut-pic"} id="tut-arena-img" src={arena_img}/></div>
                    <div className="grid-item" id="tut-arena-text"><div className={"tut-text"}>Visit the Arena to compete against your friends</div></div>

                </div>

                <button className="large-button" id="begin-btn" onPointerDown={(event) => this.closeOnClick(event)}>Got it, thanks!</button>

                {/*<div> You've just entered a solar system filled with exciting planets! Use arrow keys or WASD to fly your rocket around and explore! </div>*/}


            </div>
            </div>
        );
    }}

export default Menu;
