import React, { Component } from 'react';
import './css/NavBar.scss';
import Menu from './Menu';
import Customize from "./Customize";
import Shop from "./shop/Shop"
import {MESSAGE_TYPE, socket} from "./App";
import Tutorial from "./Tutorial";

/**
 * Models the navigation bar for the world.
 *
 * Expects the following props:
 *          @prop currRocket, the value of the current rocket enabled.
 *          @prop worldRef, a reference to the World component currently
 *          being displayed.
 *          @prop username, the player's current username.
 *          @prop coins, player's current coins
 *          @prop rocketOptions, rockets avaliable to the players
 *          @prop backgroundVolume, soundVolume, functions to set the
 *          background and sound volume.
 */
class NavBar extends Component {


    constructor(props) {
        super(props);
        this.state =  {
            settings: false,
            customize: false,
            nav: false,
            shop:false,
            mao: false,
            tutorial: false,
            bgVolume: .5,
            soundVolume: .5
        }

    }


    toggleSettings = () => {

        this.setState({nav: false, map: false, customize: false,shop:false, settings: !this.state.settings});

        if (this.state.settings) {
            document.getElementById("world-view").focus();
        }

    };


    toggleCustomize = () => {

        this.setState({settings: false, nav: false, map: false, shop:false, customize: !this.state.customize});

        if (this.state.settings === false) {
          document.getElementById("world-view").focus();
        }

    };

    toggleShop = () => {

        this.setState({settings: false, nav: false, map: false, customize: false, shop:!this.state.shop});

        if (this.state.settings === false) {
          document.getElementById("world-view").focus();
        }

    };

    toggleTutorial = () => {
        this.setState({settings: false, nav: false, map: false, customize: false, tutorial:!this.state.tutorial});

        if (this.state.settings === false) {
            document.getElementById("world-view").focus();
        }

    };

    rocketSubmit  = (rocket) => {
        this.toggleCustomize();
        if (typeof rocket !== "string") {
            return;
        }



        let toSend = {
            type: MESSAGE_TYPE.ROCKET_CHANGE,
            payload: {
                color: rocket,
                prev: this.props.currRocket
            }
        };

        socket.send(JSON.stringify(toSend));

    };

    changeBackgroundVolume = (vol) => {
        this.props.backgroundVolume(vol);
        this.setState({bgVolume: vol});

    };


    changeSoundVolume = (vol) => {
        this.props.soundVolume(vol);
        this.setState({soundVolume: vol});

    };


    toggleMap = () => {
        this.setState({settings: false, nav: false, map: !this.state.map, customize: false, tutorial:false});
        document.getElementById("world-view").focus();

    };

    render() {
        let mapTranslate = {
            top: 100 + "px",
            left: 100 + "px"
        };

        if (this.props.worldRef.current) {
            const rocketPos = this.props.worldRef.current.getRocketPos();
            mapTranslate = {
                top: Math.floor((rocketPos.y + 1000) * 700 / 2000) + "px",
                left:Math.floor((rocketPos.x + 600) * 700/ 2000) + "px"
            };
        }



        return (
            <div className="NavBar" onClick={this.onClick}>
                <ul className={"nav-list"}>
                    <li className={"name-li"}>{this.props.username}</li>
                    <li className={"money-li clickable"} onClick={this.toggleShop} ><i className="fas fa-coins"></i> {this.props.coins}</li>
                    <li className={"map-li clickable"} onClick={this.toggleMap}><i className="far fa-compass"></i></li>
                    <li className={"customize-li clickable"} onClick={this.toggleCustomize}><i className="fas fa-palette"></i> </li>
                    <li id={"settings-li"} className={"clickable"} onClick={this.toggleSettings}><i className="fas fa-cog"></i>
                    </li>
                    <li className={"tutorial-li clickable"} onClick={this.toggleTutorial}>?</li>

                </ul>
                {this.state.map ? <div className={"map"}>
                    <button className={"close-button"} id={"map-close"} onClick={this.toggleMap}>
                        <i className="fas fa-times"></i>
                    </button>
                    <div className={"player-pos"} style={mapTranslate}>
                        <div className={"dot"}></div>
                        <div className={"dot-label"}>You are here</div>
                    </div>
                    <h2>Welcome to the Dataverse</h2>
                </div>: null}
                {this.state.settings ?
                    <Menu name={this.props.username} bgVol={Math.floor(this.state.bgVolume * 100)} soundVol={Math.floor(this.state.soundVolume * 100)}
                          backgroundVolume={this.changeBackgroundVolume} soundVolume={this.changeSoundVolume} close={this.toggleSettings}/>: null}
                {this.state.customize ?
                    <Customize options={this.props.rocket_options} close={this.rocketSubmit}/>: null}
                {this.state.shop ?
                    <Shop moneyAmount = {this.props.coins} close={this.toggleShop}/>: null}
                {this.state.tutorial?
                <Tutorial onClose={this.toggleTutorial} />: null }

            </div>
        );
    }}



export default NavBar;
