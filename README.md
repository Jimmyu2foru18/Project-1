# Multi-User Chat Room Application

A Java-based chat application supporting multiple chat rooms with real-time message broadcasting and advanced room management features.

## Features

- Multiple chat rooms support
- Real-time message broadcasting
- User presence management
- Command-based interaction
- Concurrent client handling
- Room topics and descriptions
- Timestamped messages
- Color-coded message types
- Room management system

## Commands

- `/join <room>` - Join a specific chat room
- `/leave` - Leave current room
- `/list` - List available rooms
- `/users` - Show users in current room
- `/topic <text>` - Set room topic
- `/info` - Display current room information
- `/help` - Display available commands
- `/quit` - Exit the application

## Message Color Coding

- 🟢 Green: User join notifications
- 🟡 Yellow: User leave notifications
- 🔵 Blue: System information and commands
- ⚪ White: Regular chat messages

## Setup Instructions

1. Compile the source files:
```bash
javac Server.java Client.java
```

2. Start the server:
```bash
java Server
```

3. Start client(s):
```bash
java Client
```

4. Follow the prompt to enter your username when connecting

## System Requirements

- Java 8 or higher
- Network connectivity for client-server communication
- Terminal/Console with ANSI color support

## Technical Architecture

- Java Socket Programming for network communication
- Thread-per-client model using ExecutorService
- ConcurrentHashMap for thread-safe room management
- Buffered I/O for improved performance
- Non-blocking message handling

## Performance Features

- Optimized for multiple concurrent users
- Efficient message broadcasting using ConcurrentHashMap
- Memory-efficient data structures
- Connection pooling with ExecutorService
- Proper resource cleanup and management
- Graceful shutdown handling

## Error Handling

- Graceful handling of client disconnections
- Network error recovery
- Invalid command handling
- Room management edge cases
- Resource cleanup on shutdown
- Comprehensive exception handling

## Room Management

- Dynamic room creation
- Room topic management
- User presence tracking
- Room member listing
- Room information display
- Broadcast messaging within rooms

## Security Considerations

- Basic input validation
- Resource protection with proper cleanup
- Thread-safe operations
- Connection management

## Future Enhancements

- Private messaging
- File sharing
- Persistent chat history
- User authentication
- Room moderator features
- Message encryption
- Custom room permissions
- User profiles

## Troubleshooting

1. Connection Issues:
   - Verify server is running
   - Check port 5000 availability
   - Ensure correct localhost address

2. Display Issues:
   - Verify terminal supports ANSI colors
   - Check console output encoding

## Version History

- 1.0.0: Initial release
- 1.1.0: Added room topics and color coding
- 1.2.0: Added timestamp support
- 1.3.0: Enhanced room management features

## Acknowledgments

- Java Socket Programming
- Java Concurrent Utilities
- ANSI Terminal Colors 