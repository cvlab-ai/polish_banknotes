INSTRUCTION TO ADD METADATA

1. For me, the most suitable tool to build environment for the script was Anaconda.
2. After installation od all packages go to current folder in terminal.
3. In file we are using mean = 0 and std = 1. That's because our model is already normalized, so we need to add metadata only.
5. Don't forget to add .tflite model to folder and change the input and output in script in order to use it by yourself. 
4. In the bottom I have added the example of running python script. Running it from VScode was too problematic for me.

python ./metadata_writer_for_image_classifier.py --model_file=./model_1_0.tflite --label_file=./labels.txt --export_directory=./model_with_metadata/python ./metadata_writer_for_image_classifier.py --model_file=./model_1_0.tflite --label_file=./labels.txt --export_directory=model_with_metadata