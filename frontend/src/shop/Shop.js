import React, { Component } from 'react';
import '../css/Shop.scss';
import ShopGrid from "./ShopGrid"
import ShopItemInfo from "./ShopItemInfo"


import shop_items_data from "./shop_items_data"


class Shop extends Component {



    constructor(props) {
        super(props);
        this.setSelectedItem=this.setSelectedItem.bind(this)
        this.goBackToShop=this.goBackToShop.bind(this)
        this.state = {
            selectedItem:null,
            idItemMap:this.makeIDComponentMap(shop_items_data)
        }
    }


    //is there a way to determine which element was clicked?
    setSelectedItem(event){

        //gets id of the figure
        let id = event.currentTarget.id
        this.setState({
            selectedItem:this.state.idItemMap[id]
        })
    }

    makeIDComponentMap(data){
        //first make a bunch of components
        let itemComponents = data.map(item => {
            return (
                <ShopItemInfo
                key={item.itemID}
                itemID={item.itemID}
                name={item.name}
                blurb={item.blurb}
                imgUrl={item.imgUrl}
                cost={item.cost}
                back={this.goBackToShop}
                moneyAmount={this.props.moneyAmount}
                //gets this from nav
                close = {this.props.close}
                />
            )
        })
        //then iterate over them and add them to an object indexed by id
        let itemMap={}
        for(let index in itemComponents){
            let comp = itemComponents[index]
            itemMap[comp.props.itemID]=comp
        }
        return itemMap
    }

    goBackToShop(){
        this.setState({
            selectedItem:null
        })
    }




    componentDidMount() {
        document.getElementById("Shop").focus();
    }

    render() {
        return (
            <div>

            {this.state.selectedItem ?
                this.state.selectedItem  : <ShopGrid moneyAmount={this.props.moneyAmount} stateHandler={this.setSelectedItem} /*gets this from nav*/ close={this.props.close} />}
            </div>
        );
    }
}


export default Shop;
