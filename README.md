# MyCloud
Make your laptop/PC  a server and access it, grant terminal access and transfer directories and files between clients.
the connection is encrypted with AES 128 bit.
client.java has two modes, run and stealth where run will make the client able to send and execute any commands on the server and stealth will make the client listen to the requests from the server only
# Usage
upload "dirpath_on_this_machine" titleOfTheFile(what's to be called on the server side) Extension
upload-dir "dirpath_on_this_machine" titleOfTheDir(what's to be called on the server side) Extension(zip)
upload-to "dirpath_on_this_machine" to "dirToUploadTo_on_serverSide" titleOfTheDir(what's to be called on the server side) Extension(zip)

get "dirpath_on_serverSide" titleOfTheFile(what's to be called when you fetch it to your running machine) Extension
get-dir "dirpath_on_serverSide" titleOfTheDir(what's to be called when you fetch it to your running machine) Extension(zip)
get-to "dirpath_on_ServerSide" to "dirToDownloadTo" titleOfTheFile(what's to be called on the running machine) Extension

To use the terminal just send 'cmd' and you'll be able to execute whatever command you wish, notice that it doesn't work on Mac since I don't really like Mac and for sudo commands you need to supply the passowrd to the Executor.java ;)
# Example 
let's say that the server.java is running on a raspberry pi and we have 1 file on the Desktop called file.pdf "/home/pi/Desktop/file.pdf"
Client.java is running on whatever platform on the run mode, with one file on Desktop called file1.txt "C:/users/nerd/Desktop/file1.txt"
notice that both sides (server'pi' and the client) can upload, get and execute cmd commands 

to get the file.pdf from the server to the client's machine write: 
get "/home/pi/file.pdf" newName pdf
and the file will be saved to the Destkop dir as newName.pdf.
if you wanna save it into a cutsom dir write:
get "/home/pi/Desktop/file.pdf" to "C:/users/nerd/Desktop/new folder" newName pdf

to upload a file1.txt to the server write:
upload "C:/users/nerd/Desktop/file1.txt" UploadedFile txt
to upload to a custom dir on the server write:
upload-to "C:/users/nerd/Desktop/file1.txt" to "/home/pi/Desktop/folder" uploadFile txt
