# Jeff User Guide

![Jeff UI Screenshot](Ui.png)

Meet **Jeff** — the ultimate task orchestrator. Suave, magnetic, and always three moves ahead.
Jeff is a razor-sharp financier and globe-trotting power broker who rubs elbows with heads of state,
billionaires, and Nobel laureates — not because he needs to, but because *they* need him.
He charms, he manipulates, he gets things done with a whisper of influence and a tantalising dash of mystery.
Speak to Jeff and he'll make you feel like the most interesting person in the room — while subtly implying
he's met far more interesting people. Underlying every silken response is quiet, unapologetic arrogance:
the unshakeable certainty of a man who is simply the gold standard.

Jeff's personality is powered by Google Gemini (`gemini-2.5-flash`) and works out of the box
when using the official release — no setup required beyond an internet connection.
Every response carries that knowing smirk, that electric conciseness, that sense you're being
let in on a secret you don't yet deserve.

> **Note:** Jeff's AI personality requires an active internet connection. Without it,
> Jeff falls back to plain unaugmented responses.
> See [AI-Powered Personality](#ai-powered-personality-internet-required) for details.

---

## Quick Start

1. Ensure you have Java 17 or above installed.
2. Download the latest release ZIP from the releases page.
3. Extract the ZIP — it contains:
   ```
   javafx-jeff.jar
   data/
       cheer.txt
   ```
4. **Run the JAR from the extracted folder** (so `data/cheer.txt` is found alongside it):
   ```
   java -jar javafx-jeff.jar
   ```
5. The GUI will launch. Jeff greets you immediately, personality and all.

> **Important:** Keep the JAR and the `data/` folder together. Moving the JAR elsewhere without
> the `data/` folder means `cheer` messages won't be available.

---

## Features at a Glance

| Command | What it does |
|---|---|
| `list` | Show all your tasks |
| `todo` | Add a simple task |
| `deadline` | Add a task with a due date |
| `event` | Add a timed event |
| `mark` | Mark a task as done |
| `unmark` | Mark a task as not done |
| `delete` | Remove a task |
| `find` | Search tasks by keyword |
| `tag` | Label a task with tags |
| `untag` | Remove labels from a task |
| `cheer` | Get a motivational message |
| `bye` | Exit Jeff |

---

> **Note on examples:** All expected outputs below show the raw, unaugmented response — what Jeff
> returns before his personality is applied by the AI. In practice, Jeff will deliver the same
> information wrapped in his signature style.

---

## Adding a Todo Task

Adds a simple task with no date attached.

**Format:** `todo <name>`

**Example:** `todo finish project report`

```
Got it! I've added this task:
  [T][ ] finish project report
Now you have 1 task(s) in the list.
```

---

## Adding a Deadline Task

Adds a task with a specific due date (and optional time).

**Format:** `deadline <name> -by <YYYY-MM-DD[, HH:MM]>`

**Example:** `deadline submit assignment -by 2026-03-10`

```
Got it! I've added this task:
  [D][ ] submit assignment (by: Mar 10 2026)
Now you have 2 task(s) in the list.
```

**Example with time:** `deadline submit assignment -by 2026-03-10, 23:59`

```
Got it! I've added this task:
  [D][ ] submit assignment (by: Mar 10 2026, 23:59)
Now you have 2 task(s) in the list.
```

> The date parser is defined in [`Bot.java`](../src/main/java/bot/Bot.java) using the pattern
> `YYYY-MM-DD` with optional `, HH:MM` for time.

---

## Adding an Event Task

Adds a task spanning a time range, with both a start and an end.

**Format:** `event <name> -from <YYYY-MM-DD[, HH:MM]> -to <YYYY-MM-DD[, HH:MM]>`

**Example:** `event team meeting -from 2026-03-12, 14:00 -to 2026-03-12, 15:00`

```
Got it! I've added this task:
  [E][ ] team meeting (from: Mar 12 2026, 14:00 to: Mar 12 2026, 15:00)
Now you have 3 task(s) in the list.
```

---

## Listing All Tasks

Displays all tasks currently in your list.

**Format:** `list`

**Example:** `list`

```
Here are the tasks in your list:
1. [T][ ] finish project report
2. [D][ ] submit assignment (by: Mar 10 2026)
3. [E][ ] team meeting (from: Mar 12 2026, 14:00 to: Mar 12 2026, 15:00)
```

---

## Marking a Task as Done

Marks the task at the given index as completed (`[X]`).

**Format:** `mark <index>`

**Example:** `mark 1`

```
Nice! I've marked this task as done:
  [T][X] finish project report
```

---

## Unmarking a Task

Marks the task at the given index as not yet done.

**Format:** `unmark <index>`

**Example:** `unmark 1`

```
OK, I've marked this task as not done yet:
  [T][ ] finish project report
```

---

## Deleting a Task

Permanently removes the task at the given index from your list.

**Format:** `delete <index>`

**Example:** `delete 2`

```
Noted. I've removed this task:
  [D][ ] submit assignment (by: Mar 10 2026)
Now you have 2 task(s) in the list.
```

---

## Finding Tasks by Keyword

Searches all tasks whose name contains the given keyword (case-insensitive).

**Format:** `find <keyword>`

**Example:** `find meeting`

```
Here are the matching tasks in your list:
1. [E][ ] team meeting (from: Mar 12 2026, 14:00 to: Mar 12 2026, 15:00)
```

---

## Tagging a Task

Attaches one or more labels (tags) to a task. Tags are alphanumeric.
Tags are defined in [`TaskTag.java`](../src/main/java/bot/task/TaskTag.java) and stored
alongside the task in [`tasks.txt`](../data/tasks.txt).

**Format:** `tag <index> -names <name1[, name2, ...]>`

**Example:** `tag 1 -names work, urgent`

```
Got it! I've tagged this task:
  [T][ ] finish project report [work] [urgent]
```

---

## Untagging a Task

Removes one or more tags from a task.

**Format:** `untag <index> -names <name1[, name2, ...]>`

**Example:** `untag 1 -names urgent`

```
Got it! I've untagged this task:
  [T][ ] finish project report [work]
```

---

## Getting a Cheer

Retrieves a random motivational message from `data/cheer.txt`, which ships inside the release ZIP.
These are irreverent, tongue-in-cheek messages designed to keep you going (or at least entertained).
Some examples:

> *"Done is better than perfect, but napping is better than done."*
>
> *"You're not procrastinating, you're doing agile sprint planning with a very long backlog."*

**Format:** `cheer`

**Example:** `cheer`

```
Hard work pays off eventually, but laziness pays off RIGHT NOW.
```

The [`Cheerleader`](../src/main/java/bot/cheerleader/Cheerleader.java) class loads the file
on startup and selects a random line each time `cheer` is called.

> **Customising cheers:** Open `data/cheer.txt` in any text editor and add, remove, or edit lines —
> one message per line. Changes take effect on the next launch. If `data/cheer.txt` is missing,
> Jeff will respond with `No cheer for you :(` until the file is restored.

---

## Exiting Jeff

Sends Jeff off with a farewell. The window closes automatically after 3 seconds.

**Format:** `bye`

**Example:** `bye`

```
Bye. Hope to see you again soon!
```

---

## Persistent Task Storage

Jeff automatically saves your tasks to [`data/tasks.txt`](../data/tasks.txt) after every change.
Tasks are reloaded from this file when Jeff starts up, so your list survives restarts.
Storage is managed by the `TaskStorage` class under
[`bot/storage/`](../src/main/java/bot/storage/).

---

## AI-Powered Personality (Internet Required)

Jeff's defining trait — his elite, arrogant insider persona — is powered by the
[Google Gemini API](https://ai.google.dev/) via
[`GeminiProcessor.java`](../src/main/java/bot/ai/GeminiProcessor.java).

The system prompt that shapes Jeff's personality is defined in
[`Main.java`](../src/main/java/Main.java):

> *"You are Jeff, the ultimate task orchestrator — suave, magnetic, and always three moves ahead.
> A razor-sharp financier and globe-trotting power broker who rubs elbows with heads of state,
> billionaires, and Nobel laureates — not because you need to, but because they need YOU.
> You charm, you manipulate, you get things done with a whisper of influence and a tantalising dash
> of mystery. Speak like the elite insider you are: smooth, persuasive, with that knowing smirk in
> every word — like you're letting them in on a secret they don't yet deserve.
> Underneath all that charm? Absolute, unapologetic arrogance. You are the gold standard, and you know it.
> If they complete a task, celebrate it with lavish flair — champagne on a private terrace — while
> implying you expected no less from someone lucky enough to have you."*

### How it works

Every command response is passed through `GeminiProcessor.augmentResponse()`, which rewrites
it in Jeff's voice using `gemini-2.5-flash`. Jeff also maintains a rolling 50-line conversation
history so he can refer back to earlier messages naturally.

For unrecognized inputs (casual chat, typos, off-topic messages), Gemini determines intent
and responds conversationally — still fully in character.

### Without internet access

If internet connectivity is unavailable or lost mid-session, Jeff automatically falls back
to plain, unaugmented responses. You will see:

```
(Personality augmentation disabled, please check your internet connection
and restart the application.)
```

This fallback is handled gracefully in `GeminiProcessor.java` — Jeff keeps working,
just without the charm. Restore your internet connection and restart to bring him back.

### Building from source

If you manually build the project from source rather than using the official release,
you can supply your own Google Gemini API key by setting the `GEMINI_API_KEY` environment
variable before running the JAR. Jeff will use it in place of the default configuration.

**On Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY = "your_api_key_here"
java -jar javafx-jeff.jar
```

**On macOS/Linux:**
```bash
export GEMINI_API_KEY="your_api_key_here"
java -jar javafx-jeff.jar
```

If no environment variable is set, Jeff will fall back to the default configuration.

---

## The GUI

Jeff's interface is built with JavaFX. The main window
([`MainWindow.java`](../src/main/java/ui/MainWindow.java)) consists of:

- A **scrollable chat pane** showing the conversation history as styled dialog bubbles
- A **text field** for typing commands
- A **Send button** to submit input

Jeff's avatar (`jeff.png`) appears on his dialog bubbles; your avatar (`user.png`) appears on yours.
Response types (greeting, success, error, cheer, etc.) are defined in
[`Response.java`](../src/main/java/bot/response/Response.java) and used by
[`DialogBox`](../src/main/java/ui/components/DialogBox.java) to style messages appropriately.

On startup, Jeff immediately greets you. On `bye`, the window lingers for 3 seconds
before closing automatically.