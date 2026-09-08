# Ciclo de vida del ticket

```
NUEVO ──────────────► EN_PROGRESO ──────────────► TESTING ──────────────► COMPLETADO
  ▲   (Pablo crea)      │   (admin arranca)      │   (admin marca listo)      (Pablo o admin confirma)
  │                     │         ▲              │         │                       │
  │                     │         └── no funciona┘         │                       │
  │                     ▼                                  ▼                       │
  │                 ANULADO ◄─────────────────────────────┘                       │
  │              (admin, terminal)                                                 │
  └──────────────────────── reabrir ciclo (confirma el usuario) ────────────────────┘
```

## Transiciones válidas

| Desde | Hacia | Quién | Nota |
|---|---|---|---|
| NUEVO | EN_PROGRESO | admin | arranca a trabajar el ticket |
| NUEVO | ANULADO | admin | anular, no se va a hacer |
| EN_PROGRESO | TESTING | admin | deja nota/captura de qué se hizo |
| EN_PROGRESO | ANULADO | admin | anular |
| TESTING | COMPLETADO | admin o Pablo | confirma que anda |
| TESTING | EN_PROGRESO | admin o Pablo | "no funciona", vuelve atrás con nota de por qué |
| TESTING | ANULADO | admin | anular |
| COMPLETADO | NUEVO | admin | "reabrir ciclo" — el frontend pide confirmación antes de mandarla, el backend solo valida que sea una transición permitida |

`ANULADO` es terminal (no se reabre, a diferencia de `COMPLETADO`) — pedido
explícito del owner para cancelar tickets que no se van a hacer. No hay
endpoint propio: es la misma transición de `PATCH /api/tickets/{id}/estado`
que cualquier otro cambio de estado, el frontend simplemente la expone como
un botón "Anular" en vez de en la lista de "siguiente paso".

No hay estado de "cancelado/descartado" en el alcance inicial — si hace falta,
se agrega después.

Cada transición genera una fila en `historial_estado`. El estado actual del
`ticket.estado` es siempre el último `estado_nuevo` de su historial — no hay
lógica de máquina de estados formal (enum + validación simple en el service),
no vale la pena una librería de state machine para 4 estados.
