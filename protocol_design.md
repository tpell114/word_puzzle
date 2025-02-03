# DISTRIBUTED WORD PUZZLE PROTOCOL DESIGN DOCUMENT 

In this document, we outline the the list of request and reply messages and actions that follow. We also include the component and sequence diagrams as a visualization aid

## Communication Protocol
### FORMAT: <'Command code'> 'Space' <'Contents'><'Message Terminator'>
| Command Code        | Direction      |Message Description|  Action Taken |
| -------------       |-------------   |-------------      |-------------  |
|CMD_EXIT "01"        |Client -> Server|Exits the game     | Closest Client connection|
|CMD_SIGN_IN "02"     |Client -> Server|Sends username to server|Server replies with welcome message|
|CMD_LEVEL_SET "03"   |Client -> Server|Sends number of words and difficulty factor|Puzzle is created and sent by server|
|CMD_SUBMIT_GUESS "04"|Client -> Server|Sends character or word guess to server|Puzzle is updated and sent|
|CMD_CHECK_SCORE "05" |Client -> Server|                   |               |
|CMD_ABORT_GAME "06"  |Client -> Server|                   |               |
|CMD_CHECK_IF_WORD_EXISTS "07"|Client -> WordRepo|                   |               |
|CMD_ADD_WORD "08"            |Client -> WordRepo|                   |               |
|CMD_REMOVE_WORD "09"         |Client -> WordRepo|                   |               |
|CMD_SND_PUZZLE = "10"        |Server -> client  |                   |               |
|CMD_SND_SCORE = "11"         |Server -> client  |                   |               |
|CMD_SND_GAMEWIN = "12"       |Server -> client  |                   |               |
|CMD_SND_GAMELOSS = "13"      |Server -> client  |                   |               |
|CMD_SND_MISCELLANEOUS= "14"  |Server -> client  |                   |               |
|CMD_GET_RANDOM_WORD = "15"   |Server -> Wordrepo|                   |               |
|CMD_GET_STEM_WORD = "16"     |Server -> Wordrepo|                   |               |
|MSG_TERMINATOR = "\n"        |  All             |                   |               |


## Coding Standards

* Programming Language: Java
* IDE: Visual Studio Code
* Version Control: GitHub 
* Naming Conventions: Camel case

## Code Review Process
* In person meetings as well as consistent communication through discord 

## Component Diagram

![Component Diagram](https://github.com/tpell114/word_puzzle/blob/main/src/component_diagram.png)


## Sequence Diagram

![Sequence Diagram](https://github.com/tpell114/word_puzzle/blob/main/src/sequence_diagram.png)
