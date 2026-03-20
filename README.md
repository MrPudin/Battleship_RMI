# Battleship RMI

Aplicación distribuida en Java basada en **RMI (Remote Method Invocation)** para jugar a Battleship (Hundir la Flota) desde terminal.

El proyecto implementa un sistema cliente-servidor donde múltiples jugadores pueden crear salas, colocar sus barcos localmente y disputar partidas en tiempo real.

---

## Índice

- [Características principales](#características-principales)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Arquitectura y decisiones de diseño](#arquitectura-y-decisiones-de-diseño)
- [Requisitos](#requisitos)
- [Compilación](#compilación)
- [Ejecución](#ejecución)
- [Comandos del cliente](#comandos-del-cliente)
- [Roles](#roles)
- [Fases de partida](#fases-de-partida)
- [Tests](#tests)

---

## Características principales

- **Servidor RMI abierto** con registry y objeto remoto publicados en puertos configurables.
- **Clientes RMI** que se conectan al servidor y registran un callback remoto propio.
- **Múltiples salas simultáneas** con número máximo de jugadores configurable (2-4).
- **Roles**: `PLAYER`, `SPECTATOR` y `ADMIN`.
- **Fases de partida** gestionadas por el servidor: `WAITING_PLAYERS`, `PLACING_SHIPS`, `PLAYING`, `FINISHED`.
- **Tablero local en el cliente**: los barcos se colocan en el cliente y el propio cliente resuelve los impactos recibidos. El servidor nunca conoce la posición de los barcos.
- **Disparos simultáneos por turno**: todos los jugadores vivos envían su disparo antes de resolver el turno.
- **Notificaciones en tiempo real** mediante callbacks RMI: estado de sala, resultados de turno y logs de administración.
- **Tests unitarios** con JUnit 5 para modelo y servidor.

---

## Estructura del proyecto

```
src/battleship/
├── client/
│   ├── ClientMain.java          # Punto de entrada del cliente
│   ├── ClientCallbackImpl.java  # Implementación del callback remoto del cliente
│   └── TerminalUI.java          # Renderizado del tablero y eventos en terminal
│
├── dto/
│   ├── GameStatusDTO.java       # Estado de sala enviado en cada notificación
│   ├── ShipDTO.java             # Resultado de un disparo (posición + resultado)
│   ├── ShotResolutionDTO.java   # Resolución detallada de un impacto
│   └── TurnShotResultDTO.java   # Resultado de un disparo en el batch del turno
│
├── model/
│   ├── Board.java               # Tablero 10x10: colocación de barcos y resolución de disparos
│   ├── Coordinate.java          # Par fila/columna serializable
│   ├── GamePhase.java           # Enum de fases de partida
│   ├── Orientation.java         # Enum HORIZONTAL / VERTICAL
│   ├── ResultantShot.java       # Enum MISS / HIT / SUNK / REPEATED
│   ├── Room.java                # Estado de sala: usuarios, roles, turnos y fases
│   ├── RoomRole.java            # Enum PLAYER / SPECTATOR / ADMIN
│   ├── Ship.java                # Barco con celdas, impactos y detección de hundimiento
│   ├── ShipType.java            # Enum BOAT(2) / FRIGATE(3) / CRUISER(4) / AIRCRAFTCARRIER(5)
│   └── UserSession.java         # Sesión de usuario: username, callback y sala actual
│
├── remote/
│   ├── BattleshipServer.java    # Interfaz remota del servidor
│   └── ClientCallback.java      # Interfaz remota del callback del cliente
│
└── server/
    ├── BattleshipServerImpl.java # Implementación del servidor RMI
    └── ServerMain.java           # Punto de entrada del servidor

test/
└── src/battleship/
    ├── model/
    │   ├── BoardTest.java        # Tests unitarios de Board
    │   ├── RoomTest.java         # Tests unitarios de Room
    │   └── ShipTest.java         # Tests unitarios de Ship
    ├── server/
    │   └── BattleshipServerImplTest.java  # Tests de integración del servidor
    └── testdoubles/
        └── ClientCallStub.java   # Stub del callback para tests sin RMI real

compile.sh      # Script de compilación rápida
```

---

## Arquitectura y decisiones de diseño

### Tablero local en el cliente (anti-trampa)

La decisión de diseño más relevante del proyecto es que **el servidor nunca conoce la posición de los barcos de ningún jugador**. Cada cliente mantiene su propio `Board` localmente.

Cuando el servidor necesita resolver un disparo sobre un jugador, invoca el método remoto `resolveIncomingShot(row, column)` en el callback del cliente objetivo. Es el propio cliente quien procesa el impacto sobre su tablero local y devuelve el resultado (`MISS`, `HIT`, `SUNK`).

Esto garantiza que ningún otro participante, ni el servidor, pueda conocer la disposición de los barcos del rival.

```
Servidor                          Cliente B
   |                                  |
   |-- resolveIncomingShot(3,5) ----> |
   |                                  | (procesa en board local)
   |<-- ShotResolutionDTO(HIT) ------ |
```

### Disparos simultáneos por turno

A diferencia del Battleship clásico por turnos, todos los jugadores vivos envían su disparo de forma simultánea antes de que el servidor resuelva el turno. El turno se resuelve automáticamente cuando todos los jugadores han enviado su disparo, evitando bloqueos por inactividad de un jugador.

### Callbacks RMI bidireccionales

El cliente registra un objeto remoto (`ClientCallbackImpl`) en el servidor al conectarse. El servidor utiliza este callback para:

- `notifyGameStatus(GameStatusDTO)` — notificar cambios de estado de la sala.
- `notifyTurnBatch(List<TurnShotResultDTO>)` — enviar los resultados del turno a todos los participantes.
- `notifyLog(String)` — enviar mensajes de log exclusivamente a usuarios con rol `ADMIN`.
- `resolveIncomingShot(int, int)` — pedir al cliente que resuelva un disparo entrante.
- `hasLost()` — consultar al cliente si ha perdido todos sus barcos.

### Múltiples salas

El servidor gestiona un `Map<String, Room>` con `ConcurrentHashMap` para soportar múltiples salas simultáneas de forma thread-safe. Cada sala tiene su propio estado de fase, jugadores y turno actual.

---

## Requisitos

- **Java 21** (OpenJDK recomendado)
- JUnit 5 para ejecutar los tests (incluido en el módulo de test del proyecto)

---

## Compilación

Con el script incluido:

```bash
chmod +x compile.sh
./compile.sh
```

O manualmente:

```bash
rm -rf out && mkdir -p out
javac --release 21 -encoding UTF-8 -d out $(find src/battleship -name "*.java")
```

---

## Ejecución

### Servidor

```bash
java -cp out battleship.server.ServerMain [bindHost] [registryPort] [serverPort]
```

| Argumento | Descripción | Valor por defecto |
|---|---|---|
| `bindHost` | IP que anuncia el servidor | `127.0.0.1` |
| `registryPort` | Puerto del RMI Registry | `1099` |
| `serverPort` | Puerto del objeto remoto del servidor | `1100` |

Ejemplo:
```bash
java -cp out battleship.server.ServerMain 192.168.1.10 1099 1100
```

### Cliente

```bash
java -cp out battleship.client.ClientMain [serverHost] [registryPort] [clientHost] [callbackPort]
```

| Argumento | Descripción | Valor por defecto |
|---|---|---|
| `serverHost` | IP del servidor | `127.0.0.1` |
| `registryPort` | Puerto del RMI Registry del servidor | `1099` |
| `clientHost` | IP del cliente (para el callback) | `127.0.0.1` |
| `callbackPort` | Puerto del callback del cliente | `1200` |

> **Importante**: cada cliente debe usar un `callbackPort` distinto si se ejecutan en la misma máquina.

Ejemplo con dos clientes locales:
```bash
# Cliente 1
java -cp out battleship.client.ClientMain 127.0.0.1 1099 127.0.0.1 1200

# Cliente 2
java -cp out battleship.client.ClientMain 127.0.0.1 1099 127.0.0.1 1201
```

---

## Comandos del cliente

Una vez conectado, el cliente muestra un menú numérico en terminal:

| Opción | Acción | Descripción |
|---|---|---|
| `1` | NewRoom | Crea una nueva sala con nombre y número máximo de jugadores (2-4) |
| `2` | JoinRoom | Se une a una sala existente con un rol (`PLAYER`, `SPECTATOR`, `ADMIN`) |
| `3` | LeaveRoom | Abandona la sala actual |
| `4` | Colocar barcos | Coloca la flota en el tablero local (modo automático o manual) |
| `5` | Ready | Indica al servidor que los barcos están colocados y el jugador está listo |
| `6` | Disparar | Envía un disparo (fila y columna) durante la fase de juego |
| `0` | Exit | Cierra la sesión y desconecta el cliente |

### Colocación de barcos (opción 4)

Al elegir la opción 4 se ofrecen dos modos:

- **Modo automático**: coloca la flota completa en posiciones predefinidas (útil para pruebas rápidas).
- **Modo manual**: el jugador introduce fila, columna y orientación (`H`/`V`) para cada barco, con validación en tiempo real y refresco del tablero tras cada colocación.

La flota completa es:

| Barco | Tamaño |
|---|---|
| BOAT | 2 celdas |
| FRIGATE | 3 celdas |
| CRUISER | 4 celdas |
| AIRCRAFTCARRIER | 5 celdas |

---

## Roles

| Rol | Descripción |
|---|---|
| `PLAYER` | Participa en la partida. Coloca barcos, dispara y recibe disparos. Máximo `maxPlayers` por sala. |
| `SPECTATOR` | Observa la partida. Recibe notificaciones de estado pero no puede disparar ni colocar barcos. Sin límite de plazas. |
| `ADMIN` | Igual que SPECTATOR en cuanto a juego, pero además recibe todos los logs del servidor en tiempo real con formato `HH:mm [Admin:Logs] ...`. |

---

## Fases de partida

```
WAITING_PLAYERS  →  PLACING_SHIPS  →  PLAYING  →  FINISHED
      |                   |               |
  (sala llena)    (todos listos)   (un jugador queda)
```

| Fase | Descripción |
|---|---|
| `WAITING_PLAYERS` | La sala espera a que se unan todos los jugadores. |
| `PLACING_SHIPS` | Todos los jugadores colocan sus barcos localmente y confirman con `Ready`. |
| `PLAYING` | Turno a turno: todos los jugadores vivos envían su disparo y el servidor resuelve el turno simultáneamente. |
| `FINISHED` | La partida ha terminado. Se anuncia el ganador. Un jugador eliminado pasa a rol `SPECTATOR`. |

---

## Tests

El proyecto incluye tests unitarios y de integración con JUnit 5:

- **`BoardTest`** — colocación de barcos, disparos (MISS, HIT, SUNK, REPEATED), `allSunk`.
- **`ShipTest`** — generación de celdas horizontal/vertical, registro de impactos, hundimiento.
- **`RoomTest`** — gestión de usuarios, roles, fases, disparos de turno y ready.
- **`BattleshipServerImplTest`** — flujo completo del servidor usando `ClientCallStub` como doble de prueba sin RMI real: registro, salas, roles, turnos, hundimientos, derrota y logs de admin.