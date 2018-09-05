import sys
import os
import numpy as np
import tensorflow as tf
from tensorflow import keras


def GetOutputValueByType(output_type):
	output = [0,0,1,0]
	if output_type == 1:
		output = [1,0,0,0]
	elif output_type == 2:
		output = [0,1,0,0]
	elif output_type == 3:
		output = [0,0,1,0]
	elif output_type == 4:
		output = [0,0,0,1]
	return output

def GetDataset(input_file, output):
	num_lines = sum(1 for line in open(input_file))
	data = np.random.random((num_lines, 3))
	labels = np.random.random((num_lines, 4))
	with open(input_file) as fin:
		i = 0
		for line in fin:
			splitted = line.split()
			data[i][0] = splitted[0]
			data[i][1] = splitted[1]
			data[i][2] = splitted[2]
			labels[i] = output
			i=i+1
	return data,labels


if len(sys.argv) != 3:
	print("incorrect arguments");
	exit(0)

input_file = sys.argv[1]
output_type = int(sys.argv[2])
keras_file = "keras_model.h5"
backup_dir = "Backup-Models/"

bk_file_list = os.listdir(backup_dir) # dir is your directory path
number_files = len(bk_file_list)
backup_file = backup_dir+str(number_files)+keras_file

if output_type > 4:
	exit(0);

output = GetOutputValueByType(output_type)

data,labels = GetDataset(input_file, output)

model = keras.models.load_model(keras_file)
model.compile(optimizer=tf.train.AdamOptimizer(0.001),
              loss='categorical_crossentropy',
              metrics=['accuracy'])

model.fit(data, labels, epochs=4000, batch_size=2)
tf.keras.models.save_model(model, keras_file)
tf.keras.models.save_model(model, backup_file)


# # python3 TrainLates.py input.txt sample_type 


# model = keras.Sequential()
# # Adds a densely-connected layer with 64 units to the model:
# model.add(keras.layers.Dense(2, activation='relu'))
# # Add another:
# model.add(keras.layers.Dense(4, activation='relu'))
# # Add a softmax layer with 10 output units:
# model.add(keras.layers.Dense(4, activation='softmax'))

# model.compile(optimizer=tf.train.AdamOptimizer(0.001),
#               loss='categorical_crossentropy',
#               metrics=['accuracy'])

# data = np.random.random((4, 2))
# labels = np.random.random((4, 4))

# #data = [[0,0],[0,.99],[.99,0],[.99,.99]]
# #labels = [[.99,0,0,0],[0,.99,0,0],[0,0,.99,0],[0,0,0,.99]]

# data[0] = [0,0]
# data[1] = [0,1]
# data[2] = [1,0]
# data[3] = [1,1]

# labels[0] = [1,0,0,0]
# labels[1] = [0,1,0,0]
# labels[2] = [0,0,1,0]
# labels[3] = [0,0,0,1]

# print(data)
# print(data[0][0])
# model.fit(data, labels, epochs=4000, batch_size=2)


# data[0] = [10,10]
# data[1] = [10,11]
# data[2] = [10,10]
# data[3] = [10,11]

# pred = model.predict(data, batch_size=2)
# print(pred)

# keras_file = "keras_model.h5"
# tf.keras.models.save_model(model, keras_file)

# converter = tf.contrib.lite.TocoConverter.from_keras_model_file(keras_file)
# tflite_model = converter.convert()
# open("converted_model.tflite", "wb").write(tflite_model)

