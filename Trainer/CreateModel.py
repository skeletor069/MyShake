import sys
import tensorflow as tf
from tensorflow import keras

if len(sys.argv) != 2:
	print("incorrect arguments");
	exit(0)

save_file = sys.argv[1]

model = keras.Sequential()
model.add(keras.layers.Dense(96, activation='relu'))
model.add(keras.layers.Dense(128, activation='relu'))
model.add(keras.layers.Dense(4, activation='softmax'))

model.compile(optimizer=tf.train.AdamOptimizer(0.001),
              loss='categorical_crossentropy',
              metrics=['accuracy'])

tf.keras.models.save_model(model, save_file)