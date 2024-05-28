package com.example.blpslab1.service;

import com.example.blpslab1.dto.MessageNode;
import com.example.blpslab1.dto.MessageRequest;
import com.example.blpslab1.dto.MessageResponse;
import com.example.blpslab1.model.FileResponse;
import com.example.blpslab1.model.RabbitNode;
import com.example.blpslab1.subModel.FileType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.jcr.*;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import java.io.*;
import java.util.*;

@Service
@Slf4j
@Component
@EnableJms
public class FileService {
    private final JmsTemplate jmsTemplate;

    @Autowired
    public FileService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    Logger logger = LoggerFactory.getLogger(FileService.class);

    @Transactional
    public Node createNode(Session session, RabbitNode input, MultipartFile uploadFile) {
        Node node = null;
        File file = new File(Objects.requireNonNull(uploadFile.getOriginalFilename()));

        try {
            Node parentNode = session.getNodeByIdentifier(input.getParentId());
            if (parentNode != null && parentNode.hasNode(file.getName())) {
                logger.error(file.getName() + " node already exists!");
                return editNode(session, input, uploadFile);
            } else {
                try {
                    node = parentNode.addNode(file.getName(), "nt:file");
                    node.addMixin("mix:versionable");
                    node.addMixin("mix:referenceable");

                    Node content = node.addNode("jcr:content", "nt:resource");

                    InputStream inputStream = uploadFile.getInputStream();
                    Binary binary = session.getValueFactory().createBinary(inputStream);

                    content.setProperty("jcr:data", binary);
                    content.setProperty("jcr:mimeType", input.getMimeType());

                    Date now = new Date();
                    now.toInstant().toString();
                    content.setProperty("jcr:lastModified", now.toInstant().toString());

                    inputStream.close();
                    session.save();

                    VersionManager vm = session.getWorkspace().getVersionManager();
                    vm.checkin(node.getPath());

                    logger.info("File saved!");
                } catch (Exception e) {
                    logger.error("Exception caught!");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught!");
            e.printStackTrace();
        }
        return node;
    }

    @Transactional
    public boolean deleteNode(Session session, RabbitNode input) {
        try {
            Node node = session.getNodeByIdentifier(input.getFileId());
            if (node != null) {
                node.remove();
                session.save();
                return true;
            }
        } catch (Exception e) {
            logger.error("Exception caught!");
            e.printStackTrace();
        }

        return false;
    }

    public List<String> getVersionHistory(Session session, RabbitNode input) {
        List<String> versions = new ArrayList<>();
        try {
            VersionManager vm = session.getWorkspace().getVersionManager();

            Node node = session.getNodeByIdentifier(input.getFileId());
            String filePath = node.getPath();
            if (session.itemExists(filePath)) {
                VersionHistory versionHistory = vm.getVersionHistory(filePath);
                Version currentVersion = vm.getBaseVersion(filePath);
                logger.info("Current version: " + currentVersion.getName());

                VersionIterator versionIterator = versionHistory.getAllVersions();
                while (versionIterator.hasNext()) {
                    versions.add(((Version) versionIterator.next()).getName());
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught!");
            e.printStackTrace();
        }
        return versions;
    }

    @Transactional
    public Node editNode(Session session, RabbitNode input, MultipartFile uploadFile) {
        File file = new File(uploadFile.getOriginalFilename());
        Node returnNode = null;

        try {
            Node parentNode = session.getNodeByIdentifier(input.getParentId());
            if (parentNode != null && parentNode.hasNode(file.getName())) {
                VersionManager vm = session.getWorkspace().getVersionManager();

                Node fileNode = parentNode.getNode(file.getName());
                vm.checkout(fileNode.getPath());

                Node content = fileNode.getNode("jcr:content");

                InputStream is = uploadFile.getInputStream();
                Binary binary = session.getValueFactory().createBinary(is);
                content.setProperty("jcr:data", binary);

                session.save();
                is.close();

                vm.checkin(fileNode.getPath());
                returnNode = fileNode;
            }
        } catch (Exception e) {
            logger.error("Exception caught");
            e.printStackTrace();
        }

        return returnNode;
    }


    @Transactional
    public Node createFolderNode(Session session, RabbitNode input) {
        Node node = null;
        Node parentNode = null;

        try {
            parentNode = session.getNodeByIdentifier(input.getParentId());
            if (session.nodeExists(parentNode.getPath())) {
                if (!parentNode.hasNode(input.getFileName())) {
                    node = parentNode.addNode(input.getFileName(), "nt:folder");
                    node.addMixin("mix:referenceable");
                    session.save();
                    System.out.println("Folder created: " + input.getFileName());
                }
            } else {
                logger.error("Node already exists!");
            }
        } catch (Exception e) {
            logger.error("Exception caught!");
            e.printStackTrace();
        }

        return node;
    }

    public FileResponse getNode(Session session, String versionId, RabbitNode input) {
        FileResponse response = new FileResponse();

        try {
            Node file = session.getNodeByIdentifier(input.getFileId());
            if (file != null) {
                VersionManager vm = session.getWorkspace().getVersionManager();
                VersionHistory history = vm.getVersionHistory(file.getPath());
                for (VersionIterator it = history.getAllVersions(); it.hasNext(); ) {
                    Version version = (Version) it.next();
                    if (versionId.equals(version.getName())) {
                        file = version.getFrozenNode();
                        break;
                    }
                }

                logger.info("Node retrieved: " + file.getPath());

                Node fileContent = file.getNode("jcr:content");
                Binary bin = fileContent.getProperty("jcr:data").getBinary();
                InputStream stream = bin.getStream();
                byte[] bytes = IOUtils.toByteArray(stream);
                bin.dispose();
                stream.close();

                response.setBytes(bytes);
                response.setContentType(input.getMimeType());
                return response;

            } else {
                logger.error("Node does not exist!");
            }
        } catch (Exception e) {
            logger.error("Exception caught!");
            e.printStackTrace();
        }
        return response;
    }


    public void copyDir(Session session, String username, String sourceDir, String targetParentId) {
        ArrayList<MessageNode> list = new ArrayList<>();
        getDir(session, sourceDir, list);
        MessageRequest messageRequest = new MessageRequest(username, targetParentId, list);
        jmsTemplate.convertAndSend("CopyDir-request", messageRequest);
    }

    @JmsListener(destination = "CopyDir-response")
    public void pasteDir(MessageResponse response) {
        System.out.println(response.getMessage());
    }

    private void getDir(Session session, String sourceDir, ArrayList<MessageNode> list) {
        try {
            Node sourceFolder = session.getNodeByIdentifier(sourceDir);
            if (sourceFolder.isNodeType("nt:folder")) {
                MessageNode folderNode = new MessageNode(sourceFolder.getIdentifier(), FileType.FOLDER, sourceFolder.getName());
                list.add(folderNode);
                NodeIterator nodes = sourceFolder.getNodes();
                while (nodes.hasNext()) {
                    Node sourceNode = nodes.nextNode();
                    if (sourceNode.isNodeType("nt:file")) {
                        Node sourceContent = sourceNode.getNode("jcr:content");
                        MessageNode fileNode = new MessageNode(sourceNode.getIdentifier(), FileType.FILE, sourceNode.getName(), convertBinary(sourceContent.getProperty("jcr:data").getBinary()));
                        list.add(fileNode);
                    } else if (sourceNode.isNodeType("nt:folder")) {
                        getDir(session, sourceNode.getIdentifier(), list);
                    }
                }
            } else {
                logger.error("Source node is not a folder.");
            }
        } catch (RepositoryException e) {
            logger.error("Exception caught! " + e.getMessage());
        }
    }


    public org.bson.types.Binary convertBinary(Binary jcrBinary) {
        try {
            InputStream stream = jcrBinary.getStream();
            byte[] data = new byte[(int) jcrBinary.getSize()];
            int bytesRead = stream.read(data);

            if (bytesRead != data.length) {
                // Обработка случаев, когда не все данные были прочитаны
            }

            return new org.bson.types.Binary(data);
        } catch (IOException | RepositoryException e) {
            // Обработка исключений, если не удалось прочитать данные из javax.jcr.Binary
            return null;
        }
    }
    public Binary convertBinary(org.bson.types.Binary bsonBinary) {
        byte[] data = bsonBinary.getData();

        return new Binary() {
            @Override
            public InputStream getStream() throws RepositoryException {
                return new ByteArrayInputStream(data);
            }

            @Override
            public int read(byte[] b, long position) throws IOException, RepositoryException {
                // Метод read должен быть реализован в соответствии с вашими потребностями
                // Он должен читать данные из массива байтов и возвращать количество прочитанных байтов
                // position - позиция, с которой нужно начать чтение
                // b - массив байтов, в который нужно записать данные
                // В данном примере просто копируем все данные из массива data в массив b
                System.arraycopy(data, (int) position, b, 0, data.length - (int) position);
                return data.length - (int) position;
            }

            @Override
            public long getSize() throws RepositoryException {
                return data.length;
            }

            @Override
            public void dispose() {
                // Метод dispose может быть реализован по вашему усмотрению
                // Он может освобождать ресурсы, связанные с данными
            }
        };
    }
}
