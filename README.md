# Buildship sample: resource filters

Loads the resource filters from the eclipse plugin configuration and sets it up on the project.

## Installation

Menu > Install new software 

Select the `deploy` folder from this repository

Uncheck `Group items by category`

## Example usage

1) Create a Gradle project in Eclipse and create two folders: `node-modules` and `target`.

2) Add the following script to the project:

```
apply plugin: 'eclipse'

eclipse {
  project {
    resourceFilter {
      appliesTo = 'FOLDERS'
      type = 'EXCLUDE_ALL'
      recursive = false
      matcher {
        id = 'org.eclipse.ui.ide.orFilterMatcher'
        matcher {
          id = 'org.eclipse.ui.ide.multiFilter'
          arguments = '1.0-name-matches-false-false-node_modules'
        }
        matcher {
          id = 'org.eclipse.ui.ide.multiFilter'
          arguments = '1.0-name-matches-false-false-target'
        }
      }
    }
  }
}
```

3) Execute project synchronization.

After the sync the created folders should disappear.