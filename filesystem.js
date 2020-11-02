const fs = require('fs');
const path = require('path');

const getFilesForDir = (directory) => {
    let files = [];
    let dirs = [];
    
    fs.readdirSync(directory).forEach(file => {
        if (fs.lstatSync(path.resolve(directory, file)).isDirectory()) {
          dirs.push(file);
        } else {
          files.push(file);
        }
      });

      let filesAndDirs = {
          files: files,
          dirs: dirs
      }
      return filesAndDirs;
}

exports.getFilesForDir = getFilesForDir;