package org.threeform.idea.plugins

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFile
import java.util.*
import javax.swing.JComponent

/**
 * @author Andrey Turbanov
 * @since 08.04.2024
 */
class ZenEditorActivity() : ProjectActivity {
    private val activeEditors = Collections.synchronizedMap(IdentityHashMap<JComponent, ZenEditorInstall>())

    init {
        val connect = ApplicationManager.getApplication().messageBus.connect()

        connect.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                super.fileOpened(source, file)

                source.getAllEditors(file).forEach { editor ->
                    setupEditor(source.project, editor)
                }
            }

            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                super.fileClosed(source, file)
                activeEditors.forEach { (_, boot) -> boot.dispose() }
                activeEditors.clear()
            }
        })
    }

    private fun setupAllEditors(project: Project) {
        FileEditorManager.getInstance(project).allEditors.forEach {
            setupEditor(project, it)
        }
//        EditorFactory.getInstance().allEditors.forEach {
//            setupEditor(it)
//        }

    }

    private fun setupEditor(project: Project, editor: FileEditor?) {
        log.info("Setting up editor: ${editor?.file?.name ?: "null"}")
        if (editor == null || activeEditors.containsKey(editor.component)) {
            return
        }
        activeEditors[editor.component] = ZenEditorInstall(project, editor.file, editor)

    }

    override suspend fun execute(project: Project) {
        log.info("Setting up zen_editor for project: ${project.name}")
        setupAllEditors(project)
    }

    companion object {
        private val log = Logger.getInstance(
            ZenEditorInstall::class.java
        )
    }
}
