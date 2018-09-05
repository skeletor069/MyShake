import numpy as np
import tensorflow as tf
from tensorflow import keras


model = keras.Sequential()
# Adds a densely-connected layer with 64 units to the model:
model.add(keras.layers.Dense(2, activation='relu'))
# Add another:
model.add(keras.layers.Dense(4, activation='relu'))
# Add a softmax layer with 10 output units:
model.add(keras.layers.Dense(4, activation='softmax'))

model.compile(optimizer=tf.train.AdamOptimizer(0.001),
              loss='categorical_crossentropy',
              metrics=['accuracy'])

data = np.random.random((4, 2))
labels = np.random.random((4, 4))

#data = [[0,0],[0,.99],[.99,0],[.99,.99]]
#labels = [[.99,0,0,0],[0,.99,0,0],[0,0,.99,0],[0,0,0,.99]]

data[0] = [0,0]
data[1] = [0,1]
data[2] = [1,0]
data[3] = [1,1]

labels[0] = [1,0,0,0]
labels[1] = [0,1,0,0]
labels[2] = [0,0,1,0]
labels[3] = [0,0,0,1]

print(data)
print(data[0][0])
model.fit(data, labels, epochs=4000, batch_size=2)


data[0] = [10,10]
data[1] = [10,11]
data[2] = [10,10]
data[3] = [10,11]

pred = model.predict(data, batch_size=2)
print(pred)

keras_file = "keras_model.h5"
tf.keras.models.save_model(model, keras_file)

converter = tf.contrib.lite.TocoConverter.from_keras_model_file(keras_file)
tflite_model = converter.convert()
open("converted_model.tflite", "wb").write(tflite_model)

