# **Product Requirement Document: "Active Lecture" App**

## **1\. Executive Summary**

**Goal:** To maintain student attentionssss ("wakefulness") and verify cognitive engagement during Computer Science lectures. **Core Mechanism:** The instructor poses open-ended questions; students answer via mobile; an LLM provides immediate, automated feedback based on specific "Key Points" defined by the instructor.

**Philosophy:** Zero-latency feedback. The instructor acts as an observer ("Flight Controller"), not a bottleneck.

## **2\. The Core Workflow (The "Loop")**

The application relies on a fast, synchronous loop that happens multiple times per lecture:

1. **The Trigger:** Instructor activates a pre-set question or creates an Ad-Hoc one.
2. **The Alert:** Student phones vibrate/wake up. A countdown timer begins.
3. **The Input:** Student types a concise answer (constrained by character limit).
4. **The AI Triage:** The LLM compares the answer against the Instructor's "Key Points."
5. **Instant Feedback:** Student receives a Success (Green) or Partial/Fail (Orange) message with specific gap analysis.
6. **The Pulse:** Instructor sees a live dashboard update with aggregate data (e.g., "40% missed the 'Base Case' concept").

## **3\. Instructor Experience (Web/Tablet)**

### **3.1 Pre-Lecture: The Rubric Builder**

The quality of the AI feedback depends on the input structure. Questions are organized into "Lecture Decks."

* **Hierarchy:** Course \-\> Lecture (Date) \-\> Question Group.
* **Question Creator UI:**
  * **Prompt:** The actual text shown to students (e.g., *"Explain the role of the Garbage Collector."*).
  * **Model Answer:** (Hidden) A perfect reference answer for the LLM context.
  * **Required Key Points (Tags):** A list of distinct concepts the student *must* mention to pass.
    * *Example Tags:* \[Reachability\], \[Root Set\], \[Heap\].
  * **Time Limit:** Default 90 seconds.

### **3.2 Live Mode: "The Pulse Dashboard"**

During the question, the instructor sees telemetry, not a wall of text.

* **The Big Metric:** Large central percentage showing the **Class Success Rate**.
* **Key Point Breakdown (Bar Chart):**
  * Horizontal bars representing the "Key Points" defined earlier.
  * *Visual:* Green segment \= Students who included it. Red segment \= Students who missed it.
  * *Usage:* If the "Root Set" bar is mostly red, the Instructor stops and re-explains that concept verbally.
* **The Hallucination Feed:**
  * A small sidebar scrolling *only* the answers the LLM rejected.
  * Allows the instructor to spot if the LLM is being too harsh or confused.

### **3.3 Live Controls**

* **Ad-Hoc Button ("Quick Ask"):** Allows the instructor to open a generic input field without a pre-written question (e.g., for spontaneous checks).
* **"Mark All Correct" (Panic Button):** If the question was flawed or the LLM is failing everyone unfairly, this button overrides the AI and sends a "Correct" status to all students to preserve trust.

## **4\. Student Experience (Mobile App)**

### **4.1 "Listening Mode" (Default State)**

* **Visual:** Minimalist dark screen. Low battery usage.
* **Status:** "Waiting for Instructor..." or a subtle animation.
* **Purpose:** Prevents distraction. Students cannot browse old content during the lecture.

### **4.2 "Active Mode" (The Question)**

* **Trigger:** Push notification \+ haptic vibration.
* **UI Elements:**
  * Large Question Text.
  * **Countdown Timer:** Creates urgency and focus.
  * **Input Field:** Limited to \~280 characters to force concise thinking.
  * **Submit Button:** Large, easily accessible thumb target.

### **4.3 "Feedback Mode" (Post-Submit)**

The student does not wait for the instructor. The LLM responds instantly.

* **Scenario A: Success**
  * *Visual:* Green check / Success animation.
  * *Text:* "Spot on. You correctly identified \[Concept A\] and \[Concept B\]."
* **Scenario B: Partial/Fail**
  * *Visual:* Amber/Orange warning (avoid Red to reduce anxiety).
  * *Text:* "Good start, but you missed \[Concept C\]. Remember to explain *why* it happens."
  * *Action:* **"Retry" Button.** Allows the student to edit and resubmit immediately to fix their mental model.

## **5\. Technical Logic: The AI Evaluation**

### **5.1 The Prompt Structure**

The system sends the following context to the LLM API (e.g., Gemini/GPT-4o-mini) to ensure consistent grading.

**System Persona:** You are a strict but helpful T.A. for a Computer Science class.

**Context:**

* **Question:** {Instructor\_Question}
* **Must-Have Concepts:** {Array\_Of\_Tags}
* **Student Answer:** {User\_Input}

**Task:**

1. Check if {User\_Input} contains the logic behind all {Must-Have Concepts}.
2. **Output Format (JSON):**
  * is\_correct: boolean
  * missing\_concepts: array of strings (subset of Must-Have Concepts)
  * feedback: string (Max 1 sentence. Direct and constructive. Do not give the answer, just point to the gap.)

### **5.2 Latency Management**

* The system prioritizes speed.
* The dashboard updates via WebSocket/Subscription as soon as an evaluation is complete.

## **6\. Post-Lecture: The "Memorization" Loop**

To reinforce learning after the class ends.

* **The Digest:** 24 hours later, the app sends a notification.
* **Review View:** Students see:
  1. The Question.
  2. Their own Answer.
  3. The Instructor's **Model Answer** (now revealed).
  4. A brief summary of why the Model Answer is optimal.

## **7\. Database Schema (Simplified)**

* **Lectures:** id, course\_id, date, title
* **Questions:** id, lecture\_id, prompt, model\_answer, key\_points (Array)
* **Submissions:** id, user\_id, question\_id, raw\_text, llm\_feedback, is\_correct (Bool), timestamp
