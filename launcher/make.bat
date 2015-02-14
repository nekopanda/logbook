mkdir win-x86-ex
cd win-x86-ex
del logbook.exe
copy ..\winrun4j\WinRun4J.exe logbook.exe
..\winrun4j\RCEDIT.exe /C logbook.exe
..\winrun4j\RCEDIT.exe /I logbook.exe ..\logbook.ico
..\winrun4j\RCEDIT.exe /N logbook.exe ..\logbook_x86.ini
..\winrun4j\RCEDIT.exe /S logbook.exe ..\splash1.jpg
cd ..

mkdir win-x64-ex
cd win-x64-ex
del logbook.exe
copy ..\winrun4j\WinRun4J64.exe logbook.exe
..\winrun4j\RCEDIT64.exe /C logbook.exe
..\winrun4j\RCEDIT64.exe /I logbook.exe ..\logbook.ico
..\winrun4j\RCEDIT64.exe /N logbook.exe ..\logbook_x64.ini
..\winrun4j\RCEDIT64.exe /S logbook.exe ..\splash1.jpg
cd ..
