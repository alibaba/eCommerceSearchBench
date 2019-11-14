import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.python.keras.layers import Input, Embedding, LSTM, Dense
from tensorflow.python.keras.models import Model

if __name__ == '__main__':
    query_category_input = Input(shape=(1,), dtype='int32', name='query_category_input')
    user_sex_input = Input(shape=(1,), dtype='int32', name='user_sex_input')
    user_age_input = Input(shape=(1,), dtype='int32', name='user_age_input')
    user_power_input = Input(shape=(1,), dtype='int32', name='user_power_input')

    query_category_layer = Embedding(output_dim=16, input_dim=1000000, input_length=1)(query_category_input)
    user_sex_layer = Embedding(output_dim=2, input_dim=2, input_length=1)(user_sex_input)
    user_age_layer = Embedding(output_dim=8, input_dim=150, input_length=1)(user_age_input)
    user_power_layer = Embedding(output_dim=2, input_dim=7, input_length=1)(user_power_input)

    embedding_concat = keras.layers.concatenate(
        [query_category_layer, user_sex_layer, user_age_layer, user_power_layer])

    x = LSTM(32)(embedding_concat)
    x = Dense(64, activation='relu', name='Relu_1')(x)
    x = Dense(3, activation='relu', name='Relu_2')(x)

    sub_model = Model(inputs=[query_category_input, user_sex_input, user_age_input, user_power_input], outputs=[x])

    sub_model.predict([[2345], [0], [28], [6]])
    feature_input = Input(shape=(3,), dtype='float', name='feature_input')

    # x = keras.layers.dot([feature_input, x], axes=1)
    x = keras.layers.dot([feature_input, sub_model.output], axes=1)

    x = Dense(1, activation='sigmoid', name='Sigmoid')(x)

    model = Model(inputs=[query_category_input, user_sex_input, user_age_input, user_power_input, feature_input],
                  outputs=[x])

    query_category_data = np.random.randint(1000000, size=(1000, 1))
    user_sex_data = np.random.randint(2, size=(1000, 1))
    user_age_data = np.random.randint(150, size=(1000, 1))
    user_power_data = np.random.randint(7, size=(1000, 1))

    feature_data = np.random.random(size=(1000, 3))
    labels = np.random.random(size=(1000, 1))

    model.compile(optimizer='rmsprop', loss='mse')
    model.fit([query_category_data, user_sex_data, user_age_data, user_power_data, feature_data], [labels], epochs=10,
              batch_size=32)
    model.predict([[2345], [0], [28], [6], [[20.4, 23.4, 29.8]]])

    keras.utils.plot_model(model, show_layer_names=False)

    sub_model.predict([[2345], [0], [28], [6]])
    sub_model.save('ranking_weights_model.h5')
    model.predict([[1234], [1], [14], [5], [[1, 1, 1]]])
    restore_model = keras.models.load_model('ranking_weights_model.h5')
    weights = restore_model.predict([[2345], [0], [28], [5]])

    with tf.keras.backend.get_session() as sess:
        tf.saved_model.simple_save(
            sess,
            'ranking_weights_model/1',
            inputs={t.name: t for t in restore_model.input},
            outputs={t.name: t for t in restore_model.outputs})
