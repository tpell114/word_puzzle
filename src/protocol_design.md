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
|CMD_CHECK_SCORE "05" |Client -> Server|The client request to check its score|Server response with client score|
|CMD_ABORT_GAME "06"  |Client -> Server|The client asks to end the game|Server game loss response|
|CMD_CHECK_IF_WORD_EXISTS "07"|Client -> WordRepo|Asks if a certain word exists in the repo|repo response(yes/no)|
|CMD_ADD_WORD "08"            |Client -> WordRepo|Asks repo to add a word|Word repo finds index of location to insert if it does not already exist.|
|CMD_REMOVE_WORD "09"         |Client -> WordRepo|Asks repo to remove a word|Word repo searches for word and removes it|
|CMD_SND_PUZZLE "10"        |Server -> client  |The server lets the client know the contents of the message are the puzzle |The client views the puzzle in the proper format|
|CMD_SND_SCORE "11"         |Server -> client  |The server indicates it is sending the clients score|The client views score|
|CMD_SND_ERROR "99"         |Server -> client  |The server lets client know there was an error|Client reads and error occured instead of ambiguous behaviour|
|CMD_SND_GAMEWIN "12"       |Server -> client  |The server lets the client know the game has been won|The client score is increased by 1|
|CMD_SND_GAMELOSS "13"      |Server -> client  |The server lets the client know the game has been lost|The client score is decreased by 1|
|CMD_SND_MISCELLANEOUS "14"  |Server -> client  |The server send the client any message that is not required for functionality|Client just reads message as it is|
|CMD_GET_RANDOM_WORD "15"   |Server -> Wordrepo|The server asks the wordrepo to give it a random word|Random word response from repo|
|CMD_GET_STEM_WORD "16"     |Server -> Wordrepo|The server asks the repo for a stem word with minimum length according to number of words specified by user|Stem word response from repo|
|MSG_TERMINATOR "\n"        |  All             |Appended to each message sent to indicate end of message|The reader knows its done reading|


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

## Statement of Contribution
| Name        | Server     |Client|Word Repo| Going the extra mile|
| -------------       |-------------   |-------------      |-------------  |-------------  |
|Tyler       |Tyler|Tyler   | Tyler|Tyler|
|Juan       |Juan|Juan   | Juan|

