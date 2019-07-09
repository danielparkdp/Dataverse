import React, { Component } from 'react';
import '../css/ShopItemInfo.scss';
import {socket, MESSAGE_TYPE} from "../App";
import shop_planet from "../img/planets/shop_planet_v1.png";
import Planet from "../Planet";

class ShopItemInfo extends Component {

    constructor(props) {
        super(props);
        this.validatePurchase=this.validatePurchase.bind(this)
            this.state = {
                purchaseable:'notPurchase'
            }
    }

    componentDidMount() {
        this.checkIfCanBuy();

        socket.addEventListener("message", (message) => {
            const parsed = JSON.parse(message.data);
            if (parsed.type===MESSAGE_TYPE.CANBUY){
                if (parsed["valid"]===true){
                    this.setState({
                        purchaseable:'canPurchase'
                    })
                }else if (parsed["valid"]===false){
                    this.setState({
                        purchaseable:'notPurchase'
                    })
                }
            }
        });
    }

    validatePurchase(){
        let desiredItem = this.props.name;
        //send to backend for validation
        //let an alert pop up if they dont have enough funds.

        const payload = {
            item: this.props.name,
            cost: this.props.cost
        };

        const toSend = {
            type: MESSAGE_TYPE.SHOP,
            payload: payload
        };

        socket.send(JSON.stringify(toSend));
    }

    checkIfCanBuy(){
        const payload = {
            item: this.props.name,
            cost: this.props.cost
        };

        const toSend = {
            type: MESSAGE_TYPE.CANBUY,
            payload: payload
        };

        socket.send(JSON.stringify(toSend));
    }



    render(){
        return(
            <div className="ShopInfographic">
                  <Planet top={-50} left={-100} width={220} imgUrl={shop_planet} name={""}/>
                <button className={"close-button"} id={"item-close"} onClick={this.props.close}>
                    <i className="fas fa-times"></i> </button>
                <h2 id="item-name">{this.props.name}</h2>
                <p id="shop-item-cost">Cost: {this.props.cost} starbucks</p>
                <img className="item-display" alt = {this.props.name} src={this.props.imgUrl} />
                <p id="shop-item-info-blurb">{this.props.blurb}</p>
                <button className={"shop-button canPurchase"} onClick={this.props.back}>Go back</button>
                <button className={"shop-button "+this.state.purchaseable} onClick={this.validatePurchase}>Purchase</button>
            </div>
        )
    }
}
export default ShopItemInfo
