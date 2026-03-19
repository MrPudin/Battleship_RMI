# Battleship RMI

Aplicación distribuida en Java basada en **RMI (Remote Method Invocation)** para jugar a Battleship desde terminal.

El proyecto implementa un sistema cliente-servidor con:
- registro de usuarios
- creación y unión a salas
- roles de `PLAYER`, `SPECTATOR` y `ADMIN`
- callbacks RMI para estado de juego, resultados de turno y logs
- fases de partida
- colocación local de barcos en el cliente
- envío de disparos y resolución del turno

## Características principales

- **Servidor RMI abierto** con registry y objeto remoto publicados.
- **Clientes RMI** que se conectan al servidor y registran un callback remoto.
- **Salas de juego** con número máximo de jugadores.
- **Roles**:
  - `PLAYER`
  - `SPECTATOR`
  - `ADMIN`
- **Fases de partida** gestionadas por el servidor:
  - `WAITING_PLAYERS`
  - `PLACING_SHIPS`
  - `PLAYING`
  - `FINISHED`
- **Tablero local en cliente**: los barcos se colocan localmente y el cliente resuelve impactos recibidos.
- **DTO de estado** para mostrar información de la sala y de la partida.
- **Tests** en la carpeta `test/`.

## Estructura del proyecto

```text
src/battleship/
├── client/     # Cliente, callback cliente y menú de terminal
├── dto/        # DTOs de estado y datos intercambiados
├── model/      # Lógica de dominio: Room, Board, Ship, GamePhase, etc.
├── remote/     # Interfaces remotas RMI
└── server/     # Servidor e inicialización del registry

test/           # Tests del proyecto
compile.sh      # Script de compilación
