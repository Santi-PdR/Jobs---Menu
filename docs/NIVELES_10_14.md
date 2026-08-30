# Niveles 10-14

Los cinco fondos suministrados se integran como niveles reales del catalogo, no como capturas aisladas.

- Nivel 10: Area de contencion. Rojo reservado a Executores, cadenas, calor y penumbra.
- Nivel 11: El atrio. Vegetacion, luz verde y luminarias ambar.
- Nivel 12: La camara esmeralda. Piedra/metal, balizas verdes y sensacion de instalacion profunda.
- Nivel 13: El salon de guardia. Piedra calida, candil, estandartes y tunel de fondo.
- Nivel 14: La puerta de jade. Acceso monumental, balizas verdes y vacio tras la puerta.

Los fondos usan `PlantaImagen`: mantienen proporcion con recorte cover, reciben la luz de las transiciones y La Suspension, y conservan Presence, vineta y capas ambientales del sistema general.

`nivel_fijo` admite 0-14 y la rotacion automatica recorre los quince niveles.

## Audio

La musica permanece global durante los cambios de nivel y no se reinicia al pasar de una escena a otra. Los niveles 10-14 tienen una mezcla ambiental intencional construida con material sonoro ya validado del mod, por lo que se mantiene el mismo espacio acustico y el contrato mono/44100 Hz sin duplicar archivos innecesariamente.

- Nivel 10: base de catacumbas, cuerpo de ruinas, actividad de sala y eventos de cadenas/piedra.
- Nivel 11: base y caracter de invernadero, con vidrio, agua, follaje y actividad vegetal.
- Nivel 12: resonancia de cisterna, circulacion de servicio y sucesos de estructura/metal.
- Nivel 13: base y caracter de la sala de piedra, con actividad de ruinas y cadenas.
- Nivel 14: base resonante de cisterna, caracter vegetal/aire y sucesos de ruina, estandartes y puerta lejana.

Los eventos no usan el modulo numerico de los diez niveles viejos: `GestorAmbiente` resuelve explicitamente un repertorio para cada nivel nuevo, evitando que Nivel 10 herede accidentalmente el ambiente de Nivel 0.
