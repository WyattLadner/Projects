function startGame() {
    alert("OOF, this game is not in use just yet...");
}

function displayInfo() {
    alert("If you want to see more games, please contact us for more game ideas.");
}

function openLoginWindow() {
    const width = 400; // width of the new window
    const height = 300; // height of the new window
    const left = (window.innerWidth - width) / 2; // center the window horizontally
    const top = (window.innerHeight - height) / 2; // center the window vertically

    window.open('login.html', 'Login', `width=${width}, height=${height}, top=${top}, left=${left}, resizable=no`);

    sessionStorage.setItem("username", username);

    // Verify storage
    console.log("Stored username:", sessionStorage.getItem("username"));
    alert(`Welcome, ${username}!`);
}

function openPingPong() {
    let username = sessionStorage.getItem("username");
    console.log("Retrieved username:", username); // Debugging line


    window.location.href = "pingpong.html";
}

function openTanks() {
    let username = sessionStorage.getItem("username");
    console.log("Retrieved username:", username); // Debugging line

    window.location.href = "tanks.html";
}



function goBack() {
    window.location.href = 'index.html'; // Redirects to the main page
}

// In-memory data store (e.g., for use in a simple Node.js server)
let userData = {};

// Function to register a user
function registerUser(username) {
    if (!userData[username]) {
        userData[username] = { points: 0 }; // Initialize points to 0
        console.log(`User ${username} registered.`);
    } else {
        console.log(`User ${username} already exists.`);
    }
}

// Function to update points
function updatePoints(username, points) {
    if (userData[username]) {
        userData[username].points += points;
        console.log(`User ${username} now has ${userData[username].points} points.`);
    } else {
        console.log(`User ${username} not found.`);
    }
}

// Function to get points
function getPoints(username) {
    if (userData[username]) {
        return userData[username].points;
    } else {
        return `User ${username} not found.`;
    }
}

