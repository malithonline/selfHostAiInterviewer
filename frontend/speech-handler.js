class SpeechHandler {
    constructor() {
        this.recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();
        this.synthesizer = window.speechSynthesis;
        this.isSpeaking = true;
        this.isListening = false;
        this.onTranscriptUpdate = null;
        this.onFinalTranscript = null;
        this.onStateChange = null;

        // Configure recognition
        this.recognition.continuous = true;
        this.recognition.interimResults = true;
        this.recognition.lang = 'en-US';

        // Setup recognition handlers
        this.recognition.onstart = () => {
            this.isListening = true;
            if (this.onStateChange) this.onStateChange('listening', true);
        };

        this.recognition.onend = () => {
            this.isListening = false;
            if (this.onStateChange) this.onStateChange('listening', false);
            // Restart if we're supposed to be listening
            if (this.isListening) {
                try {
                    this.recognition.start();
                } catch (error) {
                    console.error('Failed to restart recognition:', error);
                }
            }
        };

        this.recognition.onerror = this.handleSpeechError.bind(this);
        this.recognition.onresult = this.handleSpeechResult.bind(this);
    }

    handleSpeechResult(event) {
        let interimTranscript = '';
        let finalTranscript = '';

        for (let i = event.resultIndex; i < event.results.length; i++) {
            const transcript = event.results[i][0].transcript;
            if (event.results[i].isFinal) {
                finalTranscript += transcript + ' ';
                if (this.onFinalTranscript) {
                    this.onFinalTranscript(finalTranscript);
                }
            } else {
                interimTranscript += transcript;
            }
        }

        if (this.onTranscriptUpdate) {
            this.onTranscriptUpdate(finalTranscript, interimTranscript);
        }
    }

    handleSpeechError(event) {
        console.error('Speech recognition error:', event.error);
        this.isListening = false;
        if (this.onStateChange) {
            this.onStateChange('error', event.error);
        }
    }

    async startListening() {
        try {
            // Request microphone permission first
            await navigator.mediaDevices.getUserMedia({ audio: true });
            this.recognition.start();
            this.isListening = true;
        } catch (error) {
            console.error('Microphone permission denied:', error);
            if (this.onStateChange) {
                this.onStateChange('error', 'Microphone permission denied');
            }
            throw error; // Re-throw to handle in UI
        }
    }

    stopListening() {
        this.isListening = false;
        this.recognition.stop();
    }

    speak(text) {
        if (!this.isSpeaking) return;
        
        this.synthesizer.cancel(); // Cancel any ongoing speech
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.rate = 1;
        utterance.pitch = 1;
        this.synthesizer.speak(utterance);
    }

    toggleSpeaker() {
        this.isSpeaking = !this.isSpeaking;
        if (!this.isSpeaking) {
            this.synthesizer.cancel();
        }
        return this.isSpeaking;
    }
}

// Export for use in other files
window.SpeechHandler = SpeechHandler;