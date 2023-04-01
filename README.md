# PHP AI Code for PHPStorm

This plugin generates code and documentation using ChatGPT API based on the code description given by the user.

## Features
✅ The plugin generates code based on the user-provided code description.
✅ Generates a doc comment for the current function.
✅ Generates a summarized comment for the selected block of code.

## Getting Started

To use PHP AI code, you'll need to install it as a plugin in your IntelliJ editor (PHPStorm). Here are the steps:

1. Go to `Settings` -> `Plugins` -> `Marketplace` in your IntelliJ editor.
2. Search for "PHP AI code" and click on the `Install` button.
3. Once the plugin is installed, restart IntelliJ IDEA editor to activate it.
4. Retrieve your ChatGPT API key from [OpenAI](https://platform.openai.com/account/api-keys).
5. Set the API Key in `Settings` -> `Tools` -> `PHP AI Code`

## Usage

### Generating Code
To generate code using the plugin, follow these steps:

1. Write a comment with a description of the code you want to generate.
2. Place the cursor inside the comment.
3. Execute the "Generate AI Code" plugin action by going to `Code` -> `Generate` or Press `Ctrl + N` (or `Cmd + N` on a Mac) and select `Generate AI Code` from the menu.
4. The generated code will be inserted into your code editor after the comment.


Alternatively, you can select a code description and execute the `Generate AI Code` plugin action. The generated code will replace the selected text.

If you execute the `Generate AI Code` plugin action without any code description, a dialog will be displayed asking you to enter one.

### Generating Doc Comment for functions
To generate a doc comment for the current function, follow these steps:

1. Place the cursor inside the function definition.
2. Go to `Code` -> `Generate` or Press `Ctrl + N` (or `Cmd + N` on a Mac) and select `Generate AI Comment` from the menu.
3. The generated doc comment will be inserted above the function definition.

### Generating Summarized Comment for code blocks
To generate a summarized comment for the selected block of code, follow these steps:

1. Select the code block you want to generate a summarized comment for.
2. Go to `Code` -> `Generate` or Press `Ctrl + N` (or `Cmd + N` on a Mac) and select `Generate AI Comment` from the menu.
3. The generated summarized comment will be inserted above the selected code block.

## Contributing

If you'd like to contribute to PHP AI Code, please feel free to submit a pull request. We welcome contributions of all types, from bug fixes to new features.

## License

PHP AI Code is licensed under the MIT License. See the `LICENSE` file for more information.

## Contact

If you have any questions or feedback about PHP AI Code, please feel free to contact us at y.alhyane@gmail.com
