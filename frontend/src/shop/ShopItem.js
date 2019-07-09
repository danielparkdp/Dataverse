import React, { Component } from 'react';
import '../css/Shop.scss';

class ShopItem extends Component {


    constructor(props) {
        super(props);
        this.state = {
            isSelected:false
        }
        this.toggleSelected=this.toggleSelected.bind(this)
    }

    toggleSelected(){
        this.setState((prevState) => ({
            isSelected: !prevState.isSelected
          }));
    }

    render(){
        return(
        <figure className="item-for-sale" id={this.props.itemID} onClick={this.props.stateHandler}>
            <img className={"item-pic"} alt={this.props.name} src={this.props.imgUrl}/>
            <figcaption className="item-cost"><b>{this.props.cost}</b> starbucks</figcaption>
        </figure>
        )
    }
}
export default ShopItem
