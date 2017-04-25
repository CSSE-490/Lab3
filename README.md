# Playing Philosopher Problem
## Authors:
CJ Miller and Jesse Shellabarger
## How to Use
* Fast Start
    * Uses the program arguments to setup the network.
    
    `main.FastStart <Local Port> <Left Host> <Right Host> <Wait Time Before Connecting> <Starvation Time>`
* Manual Start
    * Follow the Prompts in the command line.
    Push enter again after putting in port to start the client connection processes
    
### REPL Commands
* `start` : Begins the algorithm and propagates that to the other Philosophers 
* `gui` : Opens a JFrame that allows manual switching of the local Philosopher
* `hungry` : Forces the local Philosopher into the hungry state
* `thinking` : Forces the local Philosopher into the thinking state
* `thirsty` : Forces the local Philosopher into the thirsty state

## Incentive
* GUI (via the `gui` command)
* Single Connection Between Nodes
* Scales to any number of Philosophers
