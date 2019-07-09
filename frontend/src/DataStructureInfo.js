import React, { Component } from 'react';
import './css/App.scss';
import './css/GameIntroScreen.scss';
class DataStructureInfo extends Component{
    constructor(props){
        super(props)
    }

    render(){
        let content;

        if(this.props.structure==="Binary Tree"){
        content = <div class = "description">
            <p> In Computer Science, a Binary Tree is a way of storing data. It's called a tree because it looks like an upside down tree with branches
                growing out of the trunk! The trunk has a special name, the root. Each point where the tree branches into two smaller branches is called a node, which you can think of as a fruit.
                Each node, or fruit, stores one number.
            </p>
            <img className="descPicture" src={require("./img/data-structure-desc/bin-tree-pic.png")} alt="Missing Resource"/>
            <p>A Search Tree is a special kind of tree where the fruit are sorted by specific rules. Each fruit is always smaller than any other fruit to the right of it,
                and greater than any fruit to the left of it. This means the smallest fruit always grows on the very left of the tree, and the biggest fruit on the very right!
                This makes looking for fruits of specific sizes much easier! When you are looking for a target fruit, first look at the fruit you are on right now. Is the target bigger than
                the current fruit? Go to the right. Is the target less than the current fruit? Go to the left. This way you always know exactly where you need to go to find the fruit you are looking for!
                Pretty neat, huh?
            </p>
        </div>
        }else if(this.props.structure==="(Doubly) Linked List"){
            content = <div class = "description">
            <p>In Computer Science, a Linked List is a way of storing data. Each number is stored in a "node",
                 which you can think of as cookie. Each cookie has a certain number of chocolate chips in it.
                Each cookie is connected to the cookie to the left of it and to the right of it. In a "singly linked list", you can only look at the cookies from left
                to right, and in a "doubly linked list" you can move in either direction.
            </p>
            <img className="descPicture" src={require("./img/data-structure-desc/linked-list-pic.png")} alt="Missing Resource"/>
            <p>When you look through a linked list, you have no information on where the target cookie is, you have to look through every cookie until you find it.
                Think of this like looking through a line of cookies you baked at home. Maybe you want to eat the cookie with the most chocolate chips. To find this cookie,
                you have to look at all the cookies you baked! Its very easy to add the next batch of cookies to the line though!
            </p>
            <br/>
            <p>
                The strength of a linked list is that its easy to add more "nodes" to the list.
                This also means it is hard to find specific "nodes", since you have to look at every node until you find it.
            </p>
            </div>
        }else if(this.props.structure==="Stacks and Queues"){
            content = <div class = "description">
            <p>In Computer Science, a Stack is a way of storing data. It is used when you want to take items from a pile in the opposite order that they were added.
                Think of a stack of games or books you might have at home. Some books/games you read/play a lot, and some you never use. The ones you never use are at the bottom
                of the pile and the ones you really like are at the top. This way, you can always take your favorite book/game from the top of the pile without having to look through the
                rest of the pile first!
            </p>
            {/*<img className="descPicture" src={require("./img/data-structure-desc/stack-pic.png")} alt="Missing Resource"/>*/}
            <p>A Queue is also a way of storing data. It is used when you want to take items from a line in the same order that you put them in. Think about the checkout line
                at a supermarket. Everyone pays for their groceries in the order that they got in line. In Computer Science its the same way! If you put data into a Queue
                 you have to look at everything else already in the Queue before you can take it out again.
            </p>
           {/* <img className="descPicture" src={require("./img/data-structure-desc/queue-pic.png")} alt="Missing Resource"/> */}
            </div>
        }else if(this.props.structure==="HashMap"){
            content = <div class = "description">
            <p>In Computer Science, a HashMap is a way of storing data. Every number you want to store is "hashed" and then assigned to a bucket. One way of "hashing"
                is to find the remainder of your number with another prime number. You then put the number in a bucket based on the value of the remainder.
            </p>
            {/*<img className="descPicture" src={require("./img/data-structure-desc/hashmap-pic.png")} alt="Missing Resource"/>*/}
            <p>HashMaps are known for being very efficient. To find a number, you have to "hash" it, and then look in the correct bucket based on the remainder. A computer can "hash" a number very quickly,
                so HashMaps are a great data structure for finding information in computer science!
            </p>
            </div>
        }


        return(
        <div className={"infoDiv"}>
            <h1> {this.props.structure} </h1>
            <button className={"x-button"} onClick={this.props.onClose}>X</button>
            {content}
            <button className={"large-button"} id="play-btn" onClick={this.props.onClose}> Got it! </button>
        </div>);
    }

}



export default DataStructureInfo
