let gameState = 'start';
let paddle_1 = document.querySelector('.paddle_1');
let paddle_2 = document.querySelector('.paddle_2');
let board = document.querySelector('.board');
let initial_ball = document.querySelector('.ball');
let ball = document.querySelector('.ball');
let score_1 = document.querySelector('.player_1_score');
let score_2 = document.querySelector('.player_2_score');
let message = document.querySelector('.message');
let paddle_1_coord = paddle_1.getBoundingClientRect();
let paddle_2_coord = paddle_2.getBoundingClientRect();
let initial_ball_coord = ball.getBoundingClientRect();
let ball_coord = initial_ball_coord;
let board_coord = board.getBoundingClientRect();
let paddle_common =
    document.querySelector('.paddle').getBoundingClientRect();

let dx = Math.floor(Math.random() * 4) + 3;
let dy = Math.floor(Math.random() * 4) + 3;
let dxd = Math.floor(Math.random() * 2);
let dyd = Math.floor(Math.random() * 2);
let player1Name = 'Player 1';
let player2Name = 'Player 2';


// Flags to track key states
let moveUp = false;
let moveDown = false;
let moveUpPaddle1 = false;
let moveDownPaddle1 = false;

// Handle key down to start moving paddles
document.addEventListener('keydown', (e) => {
    if (e.key == 'Enter') {
        gameState = gameState == 'start' ? 'play' : 'start';
        if (gameState == 'play') {
            message.innerHTML = 'Game Started';
            message.style.left = 42 + 'vw';
            requestAnimationFrame(() => {
                dx = Math.floor(Math.random() * 4) + 3;
                dy = Math.floor(Math.random() * 4) + 3;
                dxd = Math.floor(Math.random() * 2);
                dyd = Math.floor(Math.random() * 2);
                moveBall(dx, dy, dxd, dyd);
            });
        }
    }

    if (gameState == 'play') {
        // Paddle 1 (Player 1) controls
        if (e.key == 'w') {
            moveUpPaddle1 = true;
        }
        if (e.key == 's') {
            moveDownPaddle1 = true;
        }

        // Paddle 2 (Player 2) controls
        if (e.key == 'ArrowUp') {
            moveUp = true;
        }
        if (e.key == 'ArrowDown') {
            moveDown = true;
        }
    }
});

// Handle key up to stop moving paddles
document.addEventListener('keyup', (e) => {
    if (e.key == 'w') {
        moveUpPaddle1 = false;
    }
    if (e.key == 's') {
        moveDownPaddle1 = false;
    }

    if (e.key == 'ArrowUp') {
        moveUp = false;
    }
    if (e.key == 'ArrowDown') {
        moveDown = false;
    }
});

// Update paddle positions continuously
function updatePaddles() {
    // Paddle 1 (Player 1) movement
    if (moveUpPaddle1) {
        paddle_1.style.top =
            Math.max(
                board_coord.top,
                paddle_1_coord.top - window.innerHeight * 0.04
            ) + 'px';
        paddle_1_coord = paddle_1.getBoundingClientRect();
    }
    if (moveDownPaddle1) {
        paddle_1.style.top =
            Math.min(
                board_coord.bottom - paddle_common.height,
                paddle_1_coord.top + window.innerHeight * 0.04
            ) + 'px';
        paddle_1_coord = paddle_1.getBoundingClientRect();
    }

    // Paddle 2 (Player 2) movement
    if (moveUp) {
        paddle_2.style.top =
            Math.max(
                board_coord.top,
                paddle_2_coord.top - window.innerHeight * 0.04
            ) + 'px';
        paddle_2_coord = paddle_2.getBoundingClientRect();
    }
    if (moveDown) {
        paddle_2.style.top =
            Math.min(
                board_coord.bottom - paddle_common.height,
                paddle_2_coord.top + window.innerHeight * 0.04
            ) + 'px';
        paddle_2_coord = paddle_2.getBoundingClientRect();
    }
}

// Call updatePaddles continuously to move paddles when keys are held
setInterval(updatePaddles, 20); // Update every 20ms

// Ball movement function
function moveBall(dx, dy, dxd, dyd) {
    if (ball_coord.top <= board_coord.top) {
        dyd = 1;
    }
    if (ball_coord.bottom >= board_coord.bottom) {
        dyd = 0;
    }
    if (
        ball_coord.left <= paddle_1_coord.right &&
        ball_coord.top >= paddle_1_coord.top &&
        ball_coord.bottom <= paddle_1_coord.bottom
    ) {
        dxd = 1;
        dx = Math.floor(Math.random() * 4) + 3;
        dy = Math.floor(Math.random() * 4) + 3;
    }
    if (
        ball_coord.right >= paddle_2_coord.left &&
        ball_coord.top >= paddle_2_coord.top &&
        ball_coord.bottom <= paddle_2_coord.bottom
    ) {
        dxd = 0;
        dx = Math.floor(Math.random() * 4) + 3;
        dy = Math.floor(Math.random() * 4) + 3;
    }
    if (
        ball_coord.left <= board_coord.left ||
        ball_coord.right >= board_coord.right
    ) {
        if (ball_coord.left <= board_coord.left) {
            score_2.innerHTML = +score_2.innerHTML + 1;
        } else {
            score_1.innerHTML = +score_1.innerHTML + 1;
        }
        gameState = 'start';

        ball_coord = initial_ball_coord;
        ball.style = initial_ball.style;
        message.innerHTML = 'Press Enter to Play Pong';
        message.style.left = 38 + 'vw';
        return;
    }
    ball.style.top = ball_coord.top + dy * (dyd == 0 ? -1 : 1) + 'px';
    ball.style.left = ball_coord.left + dx * (dxd == 0 ? -1 : 1) + 'px';
    ball_coord = ball.getBoundingClientRect();
    requestAnimationFrame(() => {
        moveBall(dx, dy, dxd, dyd);
    });
}

function startGame() {
    // Get player names from input fields
    player1Name = document.getElementById('player1-name').value || 'Player 1';
    player2Name = document.getElementById('player2-name').value || 'Player 2';

    // Update the displayed names next to the scores
    document.getElementById('player1-name-display').textContent = player1Name + ':';
    document.getElementById('player2-name-display').textContent = player2Name + ':';

    // Initialize the scores to 0
    score_1.innerHTML = 0;
    score_2.innerHTML = 0;

    // Hide the name input fields and start button after the game starts
    document.querySelector('.player-names').style.display = 'none';

    // Start the game
    gameState = 'play'; // Set game state to play
    message.innerHTML = 'Game Started';
    message.style.left = 42 + 'vw';
    
    // Reset ball position and movement
    requestAnimationFrame(() => {
        dx = Math.floor(Math.random() * 4) + 3;
        dy = Math.floor(Math.random() * 4) + 3;
        dxd = Math.floor(Math.random() * 2);
        dyd = Math.floor(Math.random() * 2);
        moveBall(dx, dy, dxd, dyd);
    });
}

function goBack() {
    window.location.href = 'index.html'; // Redirects to the main page
}
