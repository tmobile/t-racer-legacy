Machine Learning Component For T-Racer
===================================

This is the ML component for T-Racer. 

What does this component do?
------------

The notebooks in this repo train supervised automated driving models for T-Racer. They take YUV-420-888 image files captured by T-Racer Controller and the servo numbers captured from human driver input and train a regression model on the servo's angle and throttle based on Convolutional Neural Networks. 


What are the notebooks
------------
The notebooks could be open to ML users who would like to try different training algorithms 

#### T-Racer CNN
Notebook using hand-built CNN with Keras 

#### T-Racer Mobilenet Transfer CNN
Transfer Learning Notebook based on Mobilenet model trained on Imagenet data. Only Dense layers are being trained with T-Racer data

#### T-Racer VGG16 Transfer CNN
Transfer Learning Notebook based on VGG16 model trained on Imagenet data. Only Dense layers are being trained with T-Racer data
