
# INSTRUCTION TO ADD METADATA
1. For me, the most suitable tool to build environment for the script was `Anaconda`.

2. After installation od all packages go to current folder in terminal.

3. Our model has a normalization layer inside, i.e. `layers.Rescaling()`, that is why we can set `mean = 0` and `std = 1` in the metadata file.

4. Don't forget to add `.tflite` model to folder and change the input and output in script in order to use it by yourself.

5. In the bottom I have added the example of running python script. Running it from VScode was too problematic for me.

`python ./metadata_writer_for_image_classifier.py --model_file=./model_with_metadata/MODEL_NAME.tflite --label_file=./labels.txt --export_directory=./model_with_metadata/python ./metadata_writer_for_image_classifier.py --model_file=./MODEL_NAME.tflite --label_file=./labels.txt --export_directory=model_with_metadata`