import React, { Component } from 'react';
import '../css/Shop.scss';
import ShopItem from "./ShopItem"
import shop_items_data from "./shop_items_data"
import shop_planet from "../img/planets/shop_planet_v1.png";
import Planet from "../Planet";

class ShopGrid extends Component {


    constructor(props) {
        super(props);
        this.shopItems= this.makeShopItemList(shop_items_data)
    }

    makeShopItemList(data){
        let itemComponents = data.map(item => {
            return (
                <ShopItem
                key={item.itemID}
                itemID={item.itemID}
                name={item.name}
                imgUrl={item.imgUrl}
                cost={item.cost}
                stateHandler={this.props.stateHandler}
                />
            )
        })
        return itemComponents
    }

    

    render(){
        return (
            <div id={"Shop"} className="Shop">
            <Planet top={-50} left={-100} width={220} imgUrl={shop_planet} name={""}/>
                <button className={"close-button"} id={"shop-close"} onClick={this.props.close}>
                    <i className="fas fa-times"></i> </button>
                <h1 id="shop-welcome" >Welcome to the shop!</h1>
                <div className="money-stats">
                    <h3 className = "money-amount"> You have {this.props.moneyAmount} starbucks</h3>
                </div>
                <div className={"items-for-sale-div"}>
                    {this.shopItems}
                </div>
            </div>
        );
    }



}

export default ShopGrid;