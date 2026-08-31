# Ciclo de vida del ticket

```
NUEVO ──────────────► EN_PROGRESO ──────────────► TESTING ──────────────► COMPLETADO
  (Pablo crea)          (admin arranca)          (admin marca listo)      (Pablo o admin confirma)
                              ▲                         │
                              └─────── no funciona ──────┘
```

## Transiciones válidas

| Desde | Hacia | Quién | Nota |
|---|---|---|---|
| NUEVO | EN_PROGRESO | admin | arranca a trabajar el ticket |
| EN_PROGRESO | TESTING | admin | deja nota/captura de qué se hizo |
| TESTING | COMPLETADO | admin o Pablo | confirma que anda |
| TESTING | EN_PROGRESO | admin o Pablo | "no funciona", vuelve atrás con nota de por qué |
| cualquiera | NUEVO | — | no existe, no se retrocede hasta el inicio |

No hay estado de "cancelado/descartado" en el alcance inicial — si hace falta,
se agrega después.

Cada transición genera una fila en `historial_estado`. El estado actual del
`ticket.estado` es siempre el último `estado_nuevo` de su historial — no hay
lógica de máquina de estados formal (enum + validación simple en el service),
no vale la pena una librería de state machine para 4 estados.
