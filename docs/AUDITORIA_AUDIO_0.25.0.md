# Auditoría de audio 0.25.0

## Resultado

El catálogo deja de ser una ranura futura y pasa a tres pistas reales e independientes: **Absurdism**, **REQUIEM** y **Upon the Hill V2**.

## Mejoras implementadas

1. Tres SoundEvents independientes.
2. Tres OGG de runtime independientes.
3. REQUIEM conserva su fuente original en `music/`.
4. Upon the Hill conserva su fuente original en `music/`.
5. No se renombra una canción como si fuera otra.
6. `stream:true` en las tres pistas.
7. Inicio aleatorio por visita.
8. No repetición inmediata al elegir siguiente pista.
9. Intervalo automático reducido a 2–4 minutos.
10. Crossfade conserva dos instancias como máximo.
11. `N` permite siguiente pista desde el main.
12. `N` se bloquea mientras ya hay crossfade.
13. Feedback sonoro positivo/negado para el salto manual.
14. Crédito usa la pista dominante real.
15. REQUIEM ya no queda hardcodeado como crédito global.
16. REQUIEM conserva `Emmy Z - Forsaken OST`.
17. Upon the Hill conserva la mención `ft. @iCosmicCoffee` recibida con el archivo.
18. Absurdism no recibe una autoría inventada.
19. HUD muestra la pista actual.
20. HUD anuncia `N>NEXT`.
21. Mute sigue separado de la selección de pista.
22. Ducking de transición se conserva.
23. Ducking de Suspensión se conserva.
24. Ducking de presencia se conserva.
25. Watchdog de instancia fantasma se conserva.
26. Recarga de recursos reconstruye audio.
27. Navegar por subpantallas no reinicia canción.
28. Gameplay mantiene hard-stop.
29. `sounds.json` queda verificable por evento/recurso.
30. El verificador exige los tres OGG empaquetados.
31. El verificador exige las dos fuentes OGG nuevas.
32. Documentación ya no menciona una segunda pista pendiente.
33. No existe descarga de audio en build.
34. Los PNG 10–17 no se modifican en este pase.
35. Música permite estéreo 44,1/48 kHz sin degradar las fuentes.
36. UI, ambiente y FX conservan mono 44,1 kHz.

## Prueba pendiente

CI puede certificar estructura, recursos y Forge build, pero la mezcla subjetiva, loudness percibido y transición audible entre las tres canciones deben comprobarse dentro de Minecraft.
