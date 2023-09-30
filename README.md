# Sakudoku

A Sudoku game under development

## TODO
- Implement first actions with undo
- Implement redo (maybe stacking cell snapshot before undo)
- Try a board filler that:
    - fills independent groups 0, 4, 8
    - calculate options for unset cells
    - random solve 2, 6
    - solves singles as a whole block (maybe before random solve 2 and 6 ?)
    - solve the rest of the board