To run the code from command line, first compile with:
```
 javac ImageDisplay.java
```

and then, you can run it with the path to the RGB file as a parameter

Usage:
```
java ImageDisplay <filePath> <width> <height> <1,2> <O1,O2,O3>
```

Usage Example: 
```
java ImageDisplay C:\Users\...\hw1_data_rgb\hw1_4_low_res.rgb 400 300 2 O3
```

Image will output to png file called `output.png`