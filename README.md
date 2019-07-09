# Dataverse 

Welcome to Dataverse, an open-world game to demonstrate the power of data structures!

Dataverse was created using React.js and and Spark for Java.

Dataverse was created and developed by Laura Wilson, Daniel Park, Camillo Saueressig, and Grace Bramley-Simmons. 


To try out Dataverse, you can visit dataverse.fun and play!


### General Design of Program:
The main part of Dataverse are the minigames that each try to model and teach a certain data structure in a fun way. As of now, there are 5 main games. These games are coded based on the generic format of a "Game" class, to allow for ease of addition for new games. This is found in the java folder under game generics. In addition, games are designed to allow for any number of players at a time, so they can compete in the Arena (more on this later). The games we currently have are as follows:

### Choco Chip Links 
A game designed to model circular, doubly linked lists by creating links of "cookies" with a certain number on them, each connected to the next one and the previous one only. Players use arrow keys to move through the list and try to find the "target" cookie, earning points for doing so! Like all others, this program was coded under the abstract game class and with the ability to accommodate any number of players, using HashMaps to distinguish player info concurrently.

### Candy Hash Saga 
A game designed to model hash maps. This game uses simple math (remainders) to sort numbers into "buckets" (based on the number's remainder when divided by 5, 7, 11). The player needs to find the bucket a target number is hidden in by clicking the buckets then searching for the target.

### Barbe-queue Rush 
A game modelling queues and stacks. The player is in charge of building burgers in a certain, given order. This order depends on whether it is desired as a stack or a queue. The player needs to use the LIFO or FIFO methods to build their burger contents so they assemble correctly on a plate!

### Build a Tree 
A game to teach how building a binary tree happens. The player has a tree and is given a number to insert into the tree. Their job is to insert it in the correct spot based on the current values in the tree by clicking a node to put the number.

### Bin-Apple Tree 
A game for learning Binary trees! Users have a binary search tree that they can look around in using the arrow keys. They are given a target number, and are tasked with finding the number within the tree. They can access parent, left and right children of the current node.

### Scoring
The score users get for each game varies; it depends on a weighted factor of how efficiently they found the target, how many targets they found, and how many tries they needed to get to the target. Users also earn "Starbucks" for playing these games, based on their score. These Starbucks can be used to purchase visual changes at the Shop planet! Some items we have right now include different rockets and a speed upgrade. In addition, their scores affect their elo rating, which we compute based on their current scores relative to the standard score expected. This elo is relevant in the arena planet.


### Arena Planet:
The arena planet is one of the large focuses of our project. Here, we used concurrent, thread-safe data structures such as concurrent sets and maps to allow for multiplayer functionality! 

We accommodate lobby making and party creating, where players can invite their friends to play with them and compete. Also, players can queue for a random opponent, which matches them with someone with similar elo (and based on the amount of time they've waited) and gives them a 1v1 game to play.

We designed it so the players in the arena play a random game because we wanted to make sure players were exposed to all the different types of data structures equally. 

Another design choice was what kinds of games to include in the arena. We settled on Hash Maps, Linked Lists, and Binary trees, since all three were dealing with Searching with data structures. We wanted the arena to model as a sort of comparison mechanism for these different structures' searching times! By having similar games, searching times could become apparent.


### Login/Logout:
We also had a fairly extensive system for login/logout. We use encryption in both the front-end and the back-end to store passwords. We used sqlite3 to manage user data, and when people logged in, we used caching with a class called InfoWrapper to manage their progress while the session was alive! We handled edge cases in login logout such as multiple people trying to log into the same account, the tab being closed/refreshed (we used Cookies so refreshing doesn't log you out), and more. We also allow for changing user/pass, and joining as a unique guest!


Beyond this, we had many other features like audio settings, minimap functionality, and educational components (links, data structure descriptions, etc) to add to our final product!

We deployed using heroku to the domain dataverse.fun. All our communication between front-end and back-end was done using Websockets and messages.


Dataverse was a large project with many components, brought together with Javascript, Java, SQL, and CSS primarily. 

Thanks for reading!

