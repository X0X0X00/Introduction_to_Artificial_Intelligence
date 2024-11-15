Hello,

Here is how to create an executable for the autochecker.

Java:

Compile your java files in terminal: javac *.java

Create a new text file (this will be your executable): vim textFile

Make your text file a shell script and add the java run code (your script should look like this) where enclosingfolder is the folder that holds your files (probably hw3), main is the name of the class your main method is in, and $1 and $2 are your arguments:

#!/bin/sh

java enclosingFolder.main $1 $2

Exit out of vim (:wq) and make the executable executable: chmod a+x textFile



Python:

Make your python file executable by adding this line to the top of your file: #!/bin/python3

Create a new text file (this will be your executable): vim textFile

Make your text file a shell script and add the python run code (your script should look like this) where file.py is the name of your python file and $1 and $2 are your arguments:

#!/bin/sh

python file.py $1 $2

Exit out of vim (:wq) and make the executable executable: chmod a+x textFile

### How to Run
以下是修复后的版本，改进了表达方式，使之更加清晰和易于理解：

---

**Hello everyone,**

We've attached a zip file that contains an autochecker for Homework 3, which will test your program on 20 different SAT problems. To use the autograder, simply drag your submission folder (the folder that includes your program) into the unzipped folder we have provided.

For the autograder to work, your program must be named "sat" and must be an executable. (It's not too difficult to turn a Python script into an executable, but it might be trickier if you're using Java.) You don't need to format your program this way for the final submission, but it *must* be an executable and follow the input/output format outlined on the homework page *exactly* for the autochecker to function properly.

Then, run the following command:

```bash
./checker <NAME OF SUBMISSION FOLDER>
```

If you want to test the program without heuristics (your program must accept the `--nounit` and `--nopure` command line arguments), use:

```bash
./checker <NAME OF SUBMISSION FOLDER> none
```

Note that running the test without heuristics may take a considerable amount of time for some of the test cases.

Please let us know if you have any questions!

**PS:** On macOS devices, you may get a popup saying the software cannot be opened because it is "malicious". To fix this, go to "Settings", select "Privacy and Security", scroll down, and click "Allow" for the popup.

Best regards,  
Aryan Raj Dhawan