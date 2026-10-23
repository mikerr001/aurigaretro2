# LingBot Map evaluation

## Finding

LingBot Map is a separate Python, PyTorch, CUDA, and GPU-oriented 3D reconstruction project. The archive does not provide a supported Android SDK or a self-contained on-device model suitable for the AURIGA app.

Its Android example streams camera JPEGs to a remote WebSocket demo service using cleartext transport and no authentication. That pattern is not safe for an accessibility camera feature: it exposes live surroundings and requires the user to trust an unspecified server, retention policy, and network path.

## Decision

Keep LingBot Map research-only and separate from AURIGA's runtime. Do not ship the sample endpoint, send camera frames, or bundle the research model weights. The archive's Apache-2.0 source license does not by itself establish the model weights' license or redistribution terms.

## Requirements before any future prototype

- A user-owned or explicitly approved server with a documented operator and cost.
- Authenticated `wss://` transport, explicit opt-in, and a visible capture/streaming state.
- A short, documented retention policy with deletion controls and no use for identity tracking.
- Data minimization, threat/privacy review, offline fallback, and field testing with blind and low-vision users.
- Separate verification of code, checkpoint, and dependency licenses.

Until those decisions are made, AURIGA's camera processing remains on-device.
