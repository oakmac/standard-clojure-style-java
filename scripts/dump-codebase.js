#!/usr/bin/env node

const fs = require('fs').promises;
const path = require('path');

const projectRootDir = path.join(__dirname, '../')

async function combineFiles(outputFile) {
    const directories = [
        path.join(projectRootDir, '../standard-clojure-style-js/lib'),
        path.join(projectRootDir, '../standard-clojure-style-js/test'),
        path.join(projectRootDir, '../standard-clojure-style-lua/'),
        path.join(projectRootDir, 'src/main/java/com/oakmac/standardclojurestyle'),
        path.join(projectRootDir, 'src/test/java/com/oakmac/standardclojurestyle'),
        path.join(projectRootDir, 'src/test/resources/')
    ];

    let combinedContent = '';

    for (const dir of directories) {
        try {
            const files = await fs.readdir(dir);
            for (const file of files) {
                const fileExt = path.extname(file)
                if (fileExt === '.java' || fileExt === '.js' || fileExt === '.lua' || fileExt === '.json') {
                    const filePath = path.join(dir, file);
                    const content = await fs.readFile(filePath, 'utf8');
                    combinedContent += '********* Start File: ' + file + '\n'
                    combinedContent += content + '\n'
                    combinedContent += '********* End File: ' + file
                    combinedContent += '\n\n'
                }
            }
        } catch (error) {
            console.error(`Error reading directory ${dir}:`, error);
        }
    }

    try {
        await fs.writeFile(outputFile, combinedContent);
        console.log(`All files combined into ${outputFile}`);
    } catch (error) {
        console.error(`Error writing to ${outputFile}:`, error);
    }
}

// Usage
const outputFilePath = path.join(projectRootDir, 'combined_codebase.txt');
combineFiles(outputFilePath);